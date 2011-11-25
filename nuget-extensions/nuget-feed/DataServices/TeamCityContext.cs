using System;
using System.Collections.Concurrent;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.ServiceMessages.Read;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<RepositoryPaths> myRepositoryPaths 
      = new Lazy<RepositoryPaths>(() => new RepositoryPaths(), true);

    private static readonly Lazy<ITeamCityServerAccessor> myAccessor 
      = new Lazy<ITeamCityServerAccessor>(() => new TeamCityServerAccessor(myRepositoryPaths.Value), true);

    private static readonly Lazy<PackageLoader> myLoader
      = new Lazy<PackageLoader>(() => new PackageLoader(), true);

    private static readonly Lazy<IServiceMessageParser> myParser
      = new Lazy<IServiceMessageParser>(() => new ServiceMessageParser(), true);

    private static readonly Lazy<TeamCityPackagesPool> myPool 
      = new Lazy<TeamCityPackagesPool>(() => new TeamCityPackagesPool(), true);

    private static LightPackageRepository CreateRepository([CanBeNull] string userId)
    {
      var remoteRepo = new RemoteRepo(
        myAccessor.Value.ForUser(userId),
        new PackagesDeserializer(myParser.Value, myLoader.Value)
        );

      return new LightPackageRepository(new CachedRepo(myPool.Value.Proxy(remoteRepo)));
    }

    private static readonly ConcurrentDictionary<string, LightPackageRepository> myCache = new ConcurrentDictionary<string, LightPackageRepository>(); 

    public static LightPackageRepository GetRepository([CanBeNull] string userId)
    {
      if (myCache.Count > 10000)
      {
        myCache.Clear();
      }

      string key = "Packages-" + (userId ?? "");

      LightPackageRepository cached;
      if (myCache.TryGetValue(key, out cached))
      {
        if (cached != null)
          return cached;
      }

      var repo = CreateRepository(userId);
      myCache[key] = repo;
      return repo;
    }

    public static ITeamCityServerAccessor TeamCityAccessor
    {
      get { return myAccessor.Value; }
    }
  }
}