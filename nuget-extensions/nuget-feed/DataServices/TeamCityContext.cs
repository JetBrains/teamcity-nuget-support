using System;
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

    public static LightPackageRepository GetRepository([CanBeNull] string userId)
    {
      Cache cache = HttpContext.Current.Cache;

      string key = "Packages-" + (userId ?? "");

      var cached = cache.Get(key) as LightPackageRepository;
      if (cached != null)
        return cached;

      cached = CreateRepository(userId);
      cache.Insert(key, cache);
      return cached;
    }

    public static ITeamCityServerAccessor TeamCityAccessor
    {
      get { return myAccessor.Value; }
    }
  }
}