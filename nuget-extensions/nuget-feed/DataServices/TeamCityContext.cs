using System;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.ServiceMessages.Read;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<LightPackageRepository> myRepo 
      = new Lazy<LightPackageRepository>(
        () => new LightPackageRepository(new RemoteRepo(new RepositoryPaths().FetchPacakgesUri, new ServiceMessageParser(), new PackageLoader())), 
        true);

    public static LightPackageRepository Repository
    {
      get { return myRepo.Value; }
    }
  }
}