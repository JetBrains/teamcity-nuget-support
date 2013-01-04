using System;
using System.Collections.Generic;
using System.IO;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;
using System.Linq;
using JetBrains.TeamCity.NuGetRunner;

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
      new AssemblyResolver(GetType().Assembly.GetAssemblyDirectory());
      var reqs = LoadRequests();
      reqs.ClearCheckResults();
      
      var sourceToRequest = reqs.Packages.GroupBy(x => x.Feed, Id, NuGetSourceComparer.Comparer);
      foreach (var sourceRequest in sourceToRequest)
      {
        ProcessPackageSource(sourceRequest.Key, sourceRequest.ToList());
      }

      SaveRequests(reqs);
    }

    private void ProcessPackageSource(INuGetSource source, List<INuGetPackage> request)
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
          ProcessPackages(source, req.FetchOption, req.Data);
        }
        catch (Exception e)
        {
          foreach (var pkg in req.Data)
            pkg.AddError(e.Message);

          System.Console.Out.WriteLine("Failed to check package sources information for URI {0}. {1}", source, e.Message);
          System.Console.Out.WriteLine(e);
        }
      }
    }

    private void ProcessPackages(INuGetSource source, PackageFetchOption fetchOptions, IEnumerable<INuGetPackage> package)
    {
      var packageToData = package
        .GroupBy(x => x.Id, Id, PACKAGE_ID_COMPARER)
        .ToDictionary(x => x.Key, Id, PACKAGE_ID_COMPARER);

      if (packageToData.Count == 0) return;

      var count = 0;
      GetAllPackages(source,
                     fetchOptions,
                     packageToData.Keys,
                     p =>
                       {
                         count++;
                         IGrouping<string, INuGetPackage> res;
                         if (!packageToData.TryGetValue(p.Id, out res)) return;

                         foreach (var r in res)
                         {
                           if (!r.VersionChecker(p)) continue;
                           r.AddEntry(new NuGetPackageEntry {Version = p.VersionString()});
                         }
                       });

      System.Console.Out.WriteLine("Scanned {0} packages for feed {1}", count, source);
    }

    private void SaveRequests(INuGetPackages reqs)
    {
      XmlSerializerHelper.Save(Response, (NuGetPackages) reqs);
    }

    private INuGetPackages LoadRequests()
    {
      if (!File.Exists(Request))
        throw new CommandLineException("Failed to find file at {0}", Request);

      return XmlSerializerHelper.Load<NuGetPackages>(Request);
    }

    private static T Id<T>(T t)
    {
      return t;
    }

    private static readonly IEqualityComparer<string> PACKAGE_ID_COMPARER = StringComparer.InvariantCultureIgnoreCase;
  }
}
