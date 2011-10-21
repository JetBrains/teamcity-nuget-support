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
      return new TeamCityQueryablePackages(FetchPackageSpec()).Query;
    }

    private ITeamCityPackagesRepo FetchPackageSpec()
    {
      return myRepo;
    }
  }
}