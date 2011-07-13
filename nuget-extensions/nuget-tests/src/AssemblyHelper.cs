using System;
using System.IO;
using System.Reflection;

namespace JetBrains.TeamCity.NuGet.Tests
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
  }
}