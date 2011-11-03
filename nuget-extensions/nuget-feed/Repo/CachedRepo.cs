using System;
using System.Collections.Generic;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class CachedRepo : ITeamCityPackagesRepo
  {
    private static readonly TimeSpan myRefreshSpan = TimeSpan.FromSeconds(5);

    private static readonly object FETCH_LOCK = new object();

    private readonly ITeamCityPackagesFetcher myFetcher;
    private ITeamCityPackagesRepo myHost = new MemoryRepo();
    private DateTime myTimeToRefresh = new DateTime(1984, 08, 11);

    public CachedRepo(ITeamCityPackagesFetcher fetcher)
    {
      myFetcher = fetcher;
    }

    private void EnsureUpToDate()
    {
      if (myTimeToRefresh > DateTime.Now) return;

      lock(FETCH_LOCK)
      {
        if (myTimeToRefresh > DateTime.Now) return;

        var memo = new MemoryRepo();
        memo.AddSpecs(myFetcher.GetAllPackages());

        myTimeToRefresh = DateTime.Now + myRefreshSpan;
        myHost = memo;
      }
    }

    private ITeamCityPackagesRepo Host
    {
      get
      {
        EnsureUpToDate();
        return myHost;
      }
    }

    public IEnumerable<TeamCityPackage> GetAllPackages()
    {
      return Host.GetAllPackages();
    }

    public IEnumerable<TeamCityPackage> FilterById(IEnumerable<string> ids)
    {
      return myHost.FilterById(ids);
    }

    public IEnumerable<TeamCityPackage> FiltetByIdLatest(IEnumerable<string> ids)
    {
      return myHost.FiltetByIdLatest(ids);
    }
  }
}