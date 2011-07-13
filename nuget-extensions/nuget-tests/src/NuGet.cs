using System;
using System.IO;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class NuGet
  {
    private static string ourCachedNuGetExe = null;
    public static string NuGetPath
    {
      get
      {
        if (ourCachedNuGetExe != null) return ourCachedNuGetExe;

        string path = typeof(NuGetRunnerTest).Assembly.GetAssemblyDirectory();

        do
        {
          foreach (var probe in new[] { "nuget.exe", "lib/nuget/1.4/nuget.exe" })
          {
            string nuget = Path.Combine(path, probe);
            if (File.Exists(nuget))
              return ourCachedNuGetExe = nuget;
          }

          path = Path.GetDirectoryName(path);
        } while (path != null);
        throw new Exception("Failed to find NuGet.exe near project");
      }

    }

  }
}