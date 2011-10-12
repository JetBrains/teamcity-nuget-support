using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public interface IRepositoryPaths
  {
    [CanBeNull]
    string TeamCityPackagesFile { get; }

    [CanBeNull]
    string PackageFilesBasePath { get; }
  }
}