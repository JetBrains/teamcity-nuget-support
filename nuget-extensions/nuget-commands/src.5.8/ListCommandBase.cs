using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;
using NuGet.Configuration;
using NuGet.Protocol;
using NuGet.Protocol.Core.Types;
using NuGet.Versioning;
using NullLogger = NuGet.Common.NullLogger;
using NullSettings = NuGet.Configuration.NullSettings;
using PackageSource = NuGet.Configuration.PackageSource;
using PackageSourceProvider = NuGet.Configuration.PackageSourceProvider;
using SemanticVersion = NuGet.SemanticVersion;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract class ListCommandBase : CommandBase
  {
    protected ListCommandBase()
    {
      ResourceProviders = new List<Lazy<INuGetResourceProvider>>(Repository.Provider.GetCoreV3());
    }

    [Import]
    public IPackageRepositoryFactory RepositoryFactoryTC { get; set; }

    protected IEnumerable<Lazy<INuGetResourceProvider>> ResourceProviders { get; }

    /// <exception cref="InvalidFeedUrlException">may be thrown on error</exception>    
    protected async Task<IEnumerable<SemanticVersion>> GetAllPackagesAsync(INuGetSource feed,
                                  PackageFetchOption fetchOption,
                                  string packageId)
    {
      await System.Console.Out.WriteLineAsync($"Checking package {packageId} on source: {feed}");

      ValidateSourceUrl(feed);

      var sourceRepo = CreateSourceRepository(feed);

      var (hasV3Service, v3Service) = await new RemoteV3FindPackageByIdResourceProvider()
        .TryCreate(sourceRepo, CancellationToken.None)
        .ConfigureAwait(false);
      if (hasV3Service && v3Service is RemoteV3FindPackageByIdResource remoteV3FindPackageByIdResource)
      {
        await System.Console.Out.WriteLineAsync($"Using V3 protocol for source: {feed}");
        return await GetPackageVersionsAsync(remoteV3FindPackageByIdResource, packageId, fetchOption);
      }

      var (hasV2Service, v2Service) = await new RemoteV2FindPackageByIdResourceProvider()
        .TryCreate(sourceRepo, CancellationToken.None)
        .ConfigureAwait(false); ;
      if (hasV2Service && v2Service is RemoteV2FindPackageByIdResource remoteV2FindPackageByIdResource)
      {
        await System.Console.Out.WriteLineAsync($"Using V2 protocol for source: {feed}");
        return await GetPackageVersionsAsync(remoteV2FindPackageByIdResource, packageId, fetchOption);
      }

      var param = Expression.Parameter(typeof(IPackage));
      var filter = QueryBuilder.GenerateQuery(fetchOption, new [] { packageId }, param);

      var repo = RepositoryFactoryTC.CreateRepository(feed.Source);

      return repo
        .GetPackages()
        .Where(Expression.Lambda<Func<IPackage, bool>>(filter, param))
        .ToList() // Executing NuGet command
        .Select(x => x.Version);
    }

    private SourceRepository CreateSourceRepository(INuGetSource feed)
    {
      var sourceRepositoryProvider = new SourceRepositoryProvider(new PackageSourceProvider(NullSettings.Instance), ResourceProviders);
      var sourceRepo = sourceRepositoryProvider.CreateRepository(
        new PackageSource(feed.Source, feed.Source)
        {
          Credentials = feed.HasCredentials
            ? PackageSourceCredential.FromUserInput(feed.Source,
              feed.Username, feed.Password, false, "basic")
            : default
        });
      return sourceRepo;
    }

    private static async Task<IEnumerable<SemanticVersion>> GetPackageVersionsAsync(FindPackageByIdResource findPackageByIdResource,
      string packageId,
      PackageFetchOption fetchOption)
    {
      using (var context = new SourceCacheContext() {NoCache = true})
      {
        var packageVersions = await findPackageByIdResource.GetAllVersionsAsync(
          packageId, 
          context,
          NullLogger.Instance, 
          CancellationToken.None)
          .ConfigureAwait(false);

        IEnumerable<NuGetVersion> result = packageVersions.OrderByDescending(x => x);
        switch (fetchOption)
        {
          case PackageFetchOption.IncludeLatest:
            result = result.Where(x => !x.IsPrerelease).Take(1);
            break;
          case PackageFetchOption.IncludeLatestAndPrerelease:
            result = result.Take(1);
            break;
        }

        return result.Select(x => SemanticVersion.Parse(x.ToFullString()));
      }
    }

    private static void ValidateSourceUrl(INuGetSource feed)
    {
      string source = feed.Source;
      Uri uri;
      try
      {
        uri = new Uri(source);
      }
      catch (Exception e)
      {
        throw new InvalidFeedUrlException(source, e.Message);
      }

      if (uri.IsFile && !Directory.Exists(uri.LocalPath))
      {
        throw new InvalidFeedUrlException(source, "Local path does not exist: " + uri.LocalPath);
      }
    }
  }
}
