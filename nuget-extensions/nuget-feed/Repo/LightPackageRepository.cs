using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class LightPackageRepository
  {
    private readonly ITeamCityPackagesRepo myRepo;

    public LightPackageRepository(ITeamCityPackagesRepo repo)
    {
      myRepo = repo;
    }

    public IQueryable<TeamCityPackage> GetPackages()
    {
      TeamCityPackage[] teamCityPackages = myRepo.GetAllPackages().ToArray();
      return teamCityPackages.AsQueryable();
//      return new TeamCityQueryablePackages(FetchPackageSpec()).Query;
    }

    private ITeamCityPackagesRepo FetchPackageSpec()
    {
      return myRepo;
    }
  }
}