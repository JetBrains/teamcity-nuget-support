using System.IO;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityPackageEntryExtensions
  {
    public static string GetPackagePath(this TeamCityPackageEntry entry, IRepositorySettings settings)
    {
      return Path.Combine(settings.PackagesBase, entry.Spec.PackageFile);
    }

    public static bool IsPackageFileExists(this TeamCityPackageEntry entry, IRepositorySettings settings)
    {
      try
      {
        return File.Exists(entry.GetPackagePath(settings));
      } catch
      {
        return false;
      }
    }
  }
}