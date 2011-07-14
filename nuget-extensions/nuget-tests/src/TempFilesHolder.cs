using System;
using System.IO;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class TempFilesHolder
  {
    public static void WithTempFile(Action<string> action)
    {
      string tmp = Path.GetTempFileName();
      try
      {
        action(tmp);
      } finally
      {
        File.Delete(tmp);
      }
    }
  }
}