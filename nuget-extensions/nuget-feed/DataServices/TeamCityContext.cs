using System;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<LightPackageRepository> myRepo 
      = new Lazy<LightPackageRepository>(() => new LightPackageRepository(new RepositorySettings()), true);

    public static LightPackageRepository Repository
    {
      get { return myRepo.Value; }
    }
  }
}