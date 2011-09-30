using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Serializable]
  public class AssemblyResolver 
  {
    private readonly object myLock = new object();
    private static AssemblyResolver myInstance;

    private readonly List<string> myPaths = new List<string>();
    private readonly Dictionary<string, Assembly> myAssemblyCache = new Dictionary<string, Assembly>();

    public AssemblyResolver(params string[] paths)
    {
      if (myInstance == null)
      {
        myInstance = this;

        AppDomain.CurrentDomain.AssemblyResolve += ResolveAssembly;
        AppDomain.CurrentDomain.AssemblyLoad += AssemblyLoad;
      }
      myInstance.AddPaths(paths);
    }

    public AssemblyResolver() : this(new string[0])
    {
      
    }

    private Assembly ResolveAssembly(Object sender, ResolveEventArgs args)
    {
      lock (myLock)
      {
        return ResolveAssemblyImpl(sender, args);
      }
    }

    private Assembly ResolveAssemblyImpl(Object sender, ResolveEventArgs args)
    {
      bool isFullName = args.Name.Contains("Version=");

      // find assembly in cache
      if (isFullName)
      {
        if (myAssemblyCache.ContainsKey(args.Name))
          // return assembly from cache
          return myAssemblyCache[args.Name];
      }
      else
        foreach (Assembly assembly in myAssemblyCache.Values)
          if (assembly.GetName(false).Name == args.Name)
            // return assembly from cache
            return assembly;

      // find assembly in probe paths
      foreach (string path in myPaths)
      {
        if (!Directory.Exists(path))
          continue;

        var assemblies = new List<string>();
        assemblies.AddRange(Directory.GetFiles(path, "*.dll"));
        assemblies.AddRange(Directory.GetFiles(path, "*.exe"));
        foreach (string assemblyFile in assemblies)
        {
          try
          {
            AssemblyName assemblyName = AssemblyName.GetAssemblyName(assemblyFile);
            if (isFullName)
            {
              if (assemblyName.FullName == args.Name)
                return Assembly.LoadFrom(assemblyFile);
            }
            else if (assemblyName.Name == args.Name)
              return Assembly.LoadFrom(assemblyFile);
          }
          catch
          {
            //NOP
          }
        }
      }
      return null;
    }

    private void AssemblyLoad(object sender, AssemblyLoadEventArgs args)
    {
      lock (myLock)
      {        
        myAssemblyCache[args.LoadedAssembly.FullName] = args.LoadedAssembly;
      }
    }

    public void AddPaths(params string[] newPaths)
    {
      if (!ReferenceEquals(myInstance, this))
      {
        myInstance.AddPaths(newPaths);
      }
      else
      {
        lock (myLock)
        {
          foreach (string path in newPaths)
          {
            if (!myPaths.Contains(path))
            {              
              myPaths.Add(path);
            }
          }
        }
      }
    }
  }
}