using System.Collections.Generic;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public interface ITeamCityPackagesRepo
  {
    IEnumerable<TeamCityPackage> GetAllPackages();
    IEnumerable<TeamCityPackage> FilterById(IEnumerable<string> ids);
    IEnumerable<TeamCityPackage> FiltetByIdLatest(IEnumerable<string> ids);
    void AddSpec(TeamCityPackageEntry entry);
  }
}