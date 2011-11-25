using System;
using System.Collections.Generic;
using System.Web;
using System.Web.Caching;
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

    private static LightPackageRepository CreateRepository([CanBeNull] string userId)
    {
      var remoteRepo = new RemoteRepo(
        myAccessor.Value.ForUser(userId),
        new PackagesDeserializer(new ServiceMessageParser(), new PackageLoader())
        );

      return new LightPackageRepository(new CachedRepo(remoteRepo));
    }

    private static readonly Dictionary<string, LightPackageRepository> myCache = new Dictionary<string, LightPackageRepository>(); 

    public static LightPackageRepository GetRepository([CanBeNull] string userId)
    {
      string key = "Packages-" + (userId ?? "");

      lock(myCache)
      {
        LightPackageRepository cached;
        if (myCache.TryGetValue(key, out cached))
          return cached;

        cached = CreateRepository(userId);
        myCache[key] = cached;
        return cached;
      }
    }

    public static ITeamCityServerAccessor TeamCityAccessor
    {
      get { return myAccessor.Value; }
    }
  }
}