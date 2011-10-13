using System;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<IRepositorySettings> mySettings
      = new Lazy<IRepositorySettings>(() => new RepositorySettings(), true);

    private static readonly Lazy<LightPackageRepository> myRepo 
      = new Lazy<LightPackageRepository>(() => new LightPackageRepository(Settings), true);

    public static LightPackageRepository Repository
    {
      get { return myRepo.Value; }
    }

    public static IRepositorySettings Settings
    {
      get { return mySettings.Value; }
    }
  }
}