using System.Collections.Generic;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public interface ITeamCityPackagesFetcher
  {
    IEnumerable<TeamCityPackage> GetAllPackages();
  }

  public interface ITeamCityPackagesRepo : ITeamCityPackagesFetcher
  {    
    IEnumerable<TeamCityPackage> FilterById(IEnumerable<string> ids);
    IEnumerable<TeamCityPackage> FiltetByIdLatest(IEnumerable<string> ids);    
  }
}