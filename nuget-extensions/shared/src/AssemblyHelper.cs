using System;
using System.IO;
using System.Reflection;

namespace JetBrains.TeamCity.NuGetRunner
{
  public static class AssemblyHelper
  {
    public static string GetAssemblyDirectory(this Assembly assembly)
    {
      return Path.GetDirectoryName(GetAssemblyPath(assembly));
    }

    public static string GetAssemblyPath(this Assembly assembly)
    {
      return new Uri(assembly.CodeBase).LocalPath;
    }

    public static string GetAssemblyDirectory(this Type type)
    {
      return GetAssemblyDirectory(type.Assembly);
    }
    
    public static string GetAssemblyPath(this Type type)
    {
      return GetAssemblyPath(type.Assembly);
    }

    public static IDisposable LockAssembly(string path)
    {
      var stream = File.Open(path, FileMode.Open, FileAccess.Read, FileShare.Read);
      return new AssemblyLocker(stream);
    }

    private class AssemblyLocker : IDisposable
    {
      private readonly FileStream myStream;

      public AssemblyLocker(FileStream stream)
      {
        myStream = stream;
      }

      public void Dispose()
      {
        myStream.Close();
      }
    }
  }
}
