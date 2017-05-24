using System;
using System.IO;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class TempFilesHolder
  {
    public static T WithTempFile<T>(Func<string, T> action)
    {
      T t = default(T);
      WithTempFile(f =>
                     {
                       t = action(f);
                     });
      return t;
    }

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

    public static T WithTempDirectory<T>(Func<string, T> action)
    {
      string tmp = Path.GetTempFileName();
      File.Delete(tmp);
      Directory.CreateDirectory(tmp);
      try
      {
        return action(tmp);
      }
      finally
      {
        try
        {
          Directory.Delete(tmp, true);
        }
        catch (Exception ex)
        {
          Console.Out.WriteLine("Exception occured while deleting tmp dir. " + ex);
        }
      }
    }

    public static void WithTempDirectory(Action<string> action)
    {
      WithTempDirectory(t =>
                          {
                            action(t);
                            return t;
                          });
    }
  }
}