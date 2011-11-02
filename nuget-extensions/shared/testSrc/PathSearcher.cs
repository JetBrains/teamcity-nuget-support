using System;
using System.IO;
using JetBrains.TeamCity.NuGetRunner;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class PathSearcher
  {
    public static Lazy<string> SearchFile(params string[] matcher)
    {
      return new Lazy<string>(()=> SearchForPath(File.Exists, matcher));
    }

    public static Lazy<string> SearchDirectory(params string[] matcher)
    {
      return new Lazy<string>(()=> SearchForPath(Directory.Exists, matcher));
    }

    private static string SearchForPath(Func<string, bool> exists,  params string[] matches)
    {
      string path = typeof(PathSearcher).Assembly.GetAssemblyDirectory();

      do
      {
        foreach (var probe in matches)
        {
          string nuget = Path.Combine(path, probe);
          if (exists(nuget))
            return nuget;
        }

        path = Path.GetDirectoryName(path);
      } while (path != null);
      throw new Exception("Failed to find " + string.Join(", ", matches));
    }
  }
}