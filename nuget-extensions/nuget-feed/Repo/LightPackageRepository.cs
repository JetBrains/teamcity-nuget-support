using System.Collections.Generic;
using System.Linq;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class LightPackageRepository
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly ITeamCityPackagesRepo myRepo;

    public LightPackageRepository(ITeamCityPackagesRepo repo)
    {
      myRepo = repo;
    }

    public IQueryable<TeamCityPackage> GetPackages()
    {
      return myRepo.GetAllPackages().AsQueryable();
//      return new TeamCityQueryablePackages(FetchPackageSpec()).Query;
    }

    private ITeamCityPackagesRepo FetchPackageSpec()
    {
      return myRepo;
    }
  }
}