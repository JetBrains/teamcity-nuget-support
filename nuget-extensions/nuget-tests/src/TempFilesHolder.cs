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

    public static void WithTempDirectory(Action<string> action)
    {
      string tmp = Path.GetTempFileName();
      File.Delete(tmp);
      Directory.CreateDirectory(tmp);
      try
      {
        action(tmp);
      }
      finally
      {
        Directory.Delete(tmp,true);
      }
    }
  }
}