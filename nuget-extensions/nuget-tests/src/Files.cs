using System;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class Files
  {
    private static readonly Lazy<string> ourCachedNuGetExe_1_4 = PathSearcher.SearchFile("nuget.exe", "lib/nuget/1.4/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_5 = PathSearcher.SearchFile("nuget.exe", "lib/nuget/1.5/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetRunnerPath = PathSearcher.SearchFile("JetBrains.TeamCity.NuGetRunner.exe", "bin/JetBrains.TeamCity.NuGetRunner.exe");
    private static readonly Lazy<string> ourLocalFeed = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed");
    
    public static string NuGetExe_1_4 { get { return ourCachedNuGetExe_1_4.Value; } }
    public static string NuGetExe_1_5 { get { return ourCachedNuGetExe_1_5.Value; } }
    public static string NuGetRunnerExe { get { return ourCachedNuGetRunnerPath.Value; } }
    public static string LocalFeed { get { return ourLocalFeed.Value; } }
  }
}