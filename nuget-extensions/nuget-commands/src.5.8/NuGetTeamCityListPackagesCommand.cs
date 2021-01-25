using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;

using System.Linq;
using System.Text;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGetRunner;
using NuGet;
using NuGet.Commands;
using NuGet.Protocol.Core.Types;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.ListPackages", "Lists packages for given xml list of packages")]
  public class NuGetTeamCityListPackagesCommand : ListCommandBase
  {
    [Option("Path to file containing package-version pairs to check for updates")]
    public string Request { get; set; }

    [Option("Path to file to write result of update check")]
    public string Response { get; set; }

    protected override void ExecuteCommandImpl()
    {
      if (string.IsNullOrEmpty(Request) || !File.Exists(Request))
      {
        var message = string.Format("Request file '{0}' was not found", Request);
        System.Console.Error.WriteLine(message);
        throw new CommandException(message);
      }
      
      if (string.IsNullOrEmpty(Response))
      {
        var message = string.Format("Response file {0} was not found", Response);
        System.Console.Error.WriteLine(message);
        throw new CommandException(message);
      }
      
      new AssemblyResolver(GetType().Assembly.GetAssemblyDirectory());

      INuGetPackages reqs;
      try
      {
        reqs = XmlSerializerHelper.Load<NuGetPackages>(Request);
        reqs.ClearCheckResults();
      }
      catch (Exception e)
      {
        throw new CommandException("Invalid request file: {0}", e.Message);
      }

      ProcessRequests(reqs).ConfigureAwait(false).GetAwaiter().GetResult();

      try
      {
        XmlSerializerHelper.Save(Response, (NuGetPackages) reqs);
      }
      catch (Exception e)
      {
        System.Console.Error.WriteLine("Unable to write response file: {0}", e.Message);
        throw;
      }
    }

    private async Task ProcessRequests(INuGetPackages reqs)
    {
      var sourceToRequest = reqs.Packages.GroupBy(x => x.Feed, Id, NuGetSourceComparer.Comparer);
      foreach (var sourceRequest in sourceToRequest)
      {
        await ProcessPackageSource(sourceRequest.Key, sourceRequest.ToList());
      }
    }

    private async Task ProcessPackageSource(INuGetSource source, List<INuGetPackage> request)
    {
      //todo: optimize query to return only required set of versions.
      foreach (var req in new[]
                              {
                                new { Data = request.Where(x => x.VersionSpec == null && x.IncludePrerelease).ToArray(), FetchOption = PackageFetchOption.IncludeLatestAndPrerelease }, 
                                new { Data = request.Where(x => x.VersionSpec == null && !x.IncludePrerelease).ToArray(), FetchOption = PackageFetchOption.IncludeLatest }, 
                                new { Data = request.Where(x => x.VersionSpec != null).ToArray(), FetchOption = PackageFetchOption.IncludeAll }
                              })
      {
        try
        {
          await ProcessPackages(source, req.FetchOption, req.Data);
        }
        catch (FatalProtocolException e)
        {
          var message = e.InnerException?.Message ?? e.Message;

          foreach (var pkg in req.Data)
            pkg.AddError(message);

          System.Console.Error.WriteLine("Failed to check package sources information for {0}: {1}", source, message);
          System.Console.Out.WriteLine(e);
        }
        catch (Exception e)
        {
          string message;
          var aggregateException = e as AggregateException;
          if (aggregateException != null)
          {
            var ae = aggregateException;
            ae.Flatten();
            var stringBuilder = new StringBuilder();
            foreach (var exception in ae.InnerExceptions)
              stringBuilder.AppendLine(exception.Message);

            message = stringBuilder.ToString();
          }
          else
            message = e.Message;

          foreach (var pkg in req.Data)
            pkg.AddError(message);

          System.Console.Error.WriteLine("Failed to check package sources information for {0}: {1}", source, message);
          System.Console.Out.WriteLine(e);
        }
      }
    }

    private async Task ProcessPackages(INuGetSource source, PackageFetchOption fetchOptions, IEnumerable<INuGetPackage> package)
    {
      var packageToData = 
        package
        .GroupBy(x => x.Id, Id, PACKAGE_ID_COMPARER)
        .ToDictionary(x => x.Key, Id, PACKAGE_ID_COMPARER);

      if (packageToData.Count == 0) return;

      var count = 0;
      foreach (var packageId in packageToData.Keys)
      {
        var versions = await GetAllPackagesAsync(source,
          fetchOptions,
          packageId);

        foreach (var version in versions)
        {
          count++;
          if (!packageToData.TryGetValue(packageId, out IGrouping<string, INuGetPackage> res)) break;

          foreach (var r in res)
          {
            if (!r.VersionChecker(version)) continue;
            r.AddEntry(new NuGetPackageEntry { Version = version.ToString() });
          }
        }
      }

      await System.Console.Out.WriteLineAsync($"Scanned {count} packages for source: {source}");
    }

    private static T Id<T>(T t)
    {
      return t;
    }

    private static readonly IEqualityComparer<string> PACKAGE_ID_COMPARER = StringComparer.InvariantCultureIgnoreCase;
  }
}
