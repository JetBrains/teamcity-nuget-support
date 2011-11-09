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
//      return new TeamCityQueryablePackages(myRepo).Query;
      return myRepo.GetAllPackages().AsQueryable();
    }
  }
}