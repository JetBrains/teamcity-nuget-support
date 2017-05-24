using System;
using System.IO;
using System.Linq;
using System.Reflection;

namespace JetBrains.TeamCity.NuGetRunner
{
  public static class NuGetRunnerExtensions {
    public static string LocateNuGetExtensionsPath(this NuGetRunner runner)
    {
      Assembly nuGetAssembly = runner.NuGetAssembly;
      var mi = nuGetAssembly.EntryPoint.DeclaringType;
      foreach (var type in new Func<Type>[] { () => mi.DeclaringType, () => nuGetAssembly.GetType("NuGet.Program") }.Select(Compute).Where(x => x != null))
      {
        var field = type.GetField("ExtensionsDirectoryRoot", BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
        if (field != null && field.FieldType == typeof(string))
        {
          var extensionsPath = field.GetValue(null) as string;
          if (extensionsPath != null)
            return extensionsPath;
        }
      }
      //This is explicit path value taken from NuGet source code
      return Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "NuGet", "Commands");
    }


    private static T Compute<T>(Func<T> func) where T : class
    {
      try
      {
        return func();
      }
      catch
      {
        return null;    
      }
    }
  }
}