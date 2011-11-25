using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class TeamCityPackagesPool
  {
    private readonly ConcurrentDictionary<string, TeamCityPackage> myPackagesCache 
      = new ConcurrentDictionary<string, TeamCityPackage>();

    private static string Key(TeamCityPackage package)
    {
      return package.Id + "$" + package.Version;
    }

    public TeamCityPackage PoolPackage(TeamCityPackage package)
    {
      CheckSize();
      return PoolInternal(package);
    }

    private void CheckSize()
    {
      if (myPackagesCache.Count > 1000000)
        myPackagesCache.Clear();
    }

    private TeamCityPackage PoolInternal(TeamCityPackage package)
    {
      string key = Key(package);
      
      TeamCityPackage result;
      if (myPackagesCache.TryGetValue(key, out result))
        return result;

      myPackagesCache[key] = package;
      return package;
    }

    private IEnumerable<TeamCityPackage> PoolAll(IEnumerable<TeamCityPackage> package)
    {
      CheckSize();
      return package.Select(PoolInternal);
    }

    public ITeamCityPackagesFetcher Proxy(ITeamCityPackagesFetcher fetcher)
    {
      return new ProxyFetcher(fetcher, this);
    }

    private class ProxyFetcher : ITeamCityPackagesFetcher
    {
      private readonly ITeamCityPackagesFetcher myFetcher;
      private readonly TeamCityPackagesPool myPool;

      public ProxyFetcher(ITeamCityPackagesFetcher fetcher, TeamCityPackagesPool pool)
      {
        myFetcher = fetcher;
        myPool = pool;
      }

      public IEnumerable<TeamCityPackage> GetAllPackages()
      {
        return myPool.PoolAll(myFetcher.GetAllPackages());
      }
    }
  }
}