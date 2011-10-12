using System;
using JetBrains.TeamCity.NuGet.Feed.Repo;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<LightPackageRepository> myRepo 
      = new Lazy<LightPackageRepository>(
        () => new LightPackageRepository(new RepositoryPaths()), 
        true);

    public static LightPackageRepository Repository
    {
      get { return myRepo.Value; }
    }
  }
}