using System;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.ServiceMessages.Read;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<RepositoryPaths> myRepositoryPaths 
      = new Lazy<RepositoryPaths>(() => new RepositoryPaths(), true);

    private static readonly Lazy<ITeamCityServerAccessor> myAccessor 
      = new Lazy<ITeamCityServerAccessor>(() => new TeamCityServerAccessor(myRepositoryPaths.Value.FetchPacakgesUri), true);

    private static readonly Lazy<LightPackageRepository> myRepo 
      = new Lazy<LightPackageRepository>(
        () => new LightPackageRepository(new RemoteRepo(myAccessor.Value, new ServiceMessageParser(), new PackageLoader())), 
        true);

    public static LightPackageRepository Repository
    {
      get { return myRepo.Value; }
    }

    public static ITeamCityServerAccessor TeamCityAccessor
    {
      get { return myAccessor.Value; }
    }
  }
}