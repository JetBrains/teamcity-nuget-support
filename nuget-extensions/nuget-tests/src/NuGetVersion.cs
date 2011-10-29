namespace JetBrains.TeamCity.NuGet.Tests
{
  public enum NuGetVersion
  {
    NuGet_1_4,
    NuGet_1_5,

    NuGet_Latest_CI,
    NuGet_CommandLine_Package_Latest
  }


  public static class NuGetVersionExtensions
  {
    public static bool Is_1_4(this NuGetVersion version)
    {
      return version == NuGetVersion.NuGet_1_4;
    }
  }

}