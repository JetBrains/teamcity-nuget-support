using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public static class PackageExtensions2
  {
    public static string VersionString(this IPackage package)
    {
      // There was a change in signature in NuGet poset 1.5. IPackage.get_Version now returns Semantic version instead of Version
      // Here is no difference here, so we can stick to dynamic object as there are tests for this method.
      return ((dynamic)package).Version.ToString(); 
    }
  }
}