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
  }
}