using System;
using System.Collections.Generic;
using JetBrains.TeamCity.NuGet.Feed.DataServices;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  public static class LINQExtensions
  {
    public static IEnumerable<T> Repeat<T>(this int count, Func<T> factory)
    {
      for (int i = 0; i < count; i++)
      {
        yield return factory();
      }
    }

    public static void Iterate(this int count, Action factory)
    {
      for (int i = 0; i < count; i++)
      {
        factory();
      }
    }


    public static TeamCityPackageEntry ToEntry(this int id)
    {
      return new TeamCityPackageEntry
               {
                 Spec = new TeamCityPackageSpec
                          {
                            BuildId = 555 + id,
                            BuildType = "btX",
                            DownloadUrl = "http://localhost",
                            PackageFile = "Package." + id + ".nupkg",
                          },
                 Package = new TeamCityPackage
                             {
                               Id = "pkg." + id + ".zzz",
                               Description = "description: " + id,
                               Version =  "1.2." + id,
                             }
               };
    }
  }
}