using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  //NOTE: Do not use any our types or libs here to avoid 
  //NOTE: Unexpected assembly load events from 
  //NOTE: Current loader
  [Serializable]
  public class AssemblyResolver
  {
    private readonly object myLock = new object();
    private static volatile AssemblyResolver myInstance;

    private readonly HashSet<string> myPaths = new HashSet<string>();
    private readonly AssemblyNameCache myAssemblyNameCache = new AssemblyNameCache();

    public AssemblyResolver(params string[] paths)
    {
      AssemblyResolver instance = myInstance;
      if (instance == null)
      {
        myInstance = this;
        instance = this;

        AppDomain.CurrentDomain.AssemblyResolve += ResolveAssembly;
        AppDomain.CurrentDomain.AssemblyLoad += AssemblyLoad;
      }
      instance.AddPaths(paths);
    }

    private Assembly ResolveAssembly(Object sender, ResolveEventArgs args)
    {
      lock (myLock)
      {
        return myAssemblyNameCache.Find(args.Name);
      }
    }

    private void AssemblyLoad(object sender, AssemblyLoadEventArgs args)
    {
      lock (myLock)
      {
        myAssemblyNameCache.AddEntry(args.LoadedAssembly);
      }
    }

    private void ReloadAssemblyFiles()
    {
      lock (myLock)
      {
        var assemblies = new List<string>();
        foreach (string path in myPaths)
        {
          if (!Directory.Exists(path)) continue;
          assemblies.AddRange(Directory.GetFiles(path, "*.dll"));
          assemblies.AddRange(Directory.GetFiles(path, "*.exe"));
        }

        foreach (string file in assemblies)
        {
          myAssemblyNameCache.AddEntry(file);
        }
      }
    }

    private void AddPaths(params string[] newPaths)
    {
      if (!ReferenceEquals(myInstance, this))
      {
        myInstance.AddPaths(newPaths);
        return;
      }

      lock (myLock)
      {
        foreach (string path in newPaths)
        {
          myPaths.Add(path);
        }
        ReloadAssemblyFiles();
      }
    }
  }

  internal class AssembyNameAwareCache<T>
  {
    private readonly Dictionary<string, T> myAssemblyFullNameCache = new Dictionary<string, T>();
    private readonly Dictionary<string, T> myAssemblyShortNameCache = new Dictionary<string, T>();

    public void AddEntry(AssemblyName name, T obj)
    {
      myAssemblyFullNameCache[name.FullName] = obj;
      myAssemblyShortNameCache[name.Name] = obj;
    }

    public T Find(string name)
    {
      bool isFullName = name.Contains("Version=");

      T result;
      return
        (isFullName ? myAssemblyFullNameCache : myAssemblyShortNameCache).TryGetValue(name, out result)
          ? result
          : default(T);
    }

    public void Remove(AssemblyName name)
    {
      myAssemblyFullNameCache.Remove(name.FullName);
      myAssemblyShortNameCache.Remove(name.Name);
    }
  }

  internal class AssemblyCache
  {
    private readonly AssembyNameAwareCache<Assembly> myCache = new AssembyNameAwareCache<Assembly>();

    public void AddEntry(Assembly assembly)
    {
      myCache.AddEntry(assembly.GetName(), assembly);
    }

    public Assembly Find(string name)
    {
      return myCache.Find(name);
    }
  }

  internal class AssemblyNameCache
  {
    private readonly LoaderCache<AssemblyName> myAssemblyNameLoader = new LoaderCache<AssemblyName>(AssemblyName.GetAssemblyName);
    private readonly LoaderCache<Assembly> myAssemblyLoader = new LoaderCache<Assembly>(Assembly.LoadFrom);
    private readonly AssembyNameAwareCache<string> myCache = new AssembyNameAwareCache<string>();
    private readonly AssemblyCache myLoadedCache = new AssemblyCache();

    public void AddEntry(string file)
    {
      AssemblyName name = myAssemblyNameLoader.Load(file);
      if (name != null)
        myCache.AddEntry(name, file);
    }

    public void AddEntry(Assembly assembly)
    {
      myCache.Remove(assembly.GetName());
      myLoadedCache.AddEntry(assembly);
    }

    public Assembly Find(string name)
    {
      Assembly assm = myLoadedCache.Find(name);
      if (assm != null) return assm;

      var file = myCache.Find(name);
      if (file == null) return null;

      assm = myAssemblyLoader.Load(file);
      if (assm != null)
        myLoadedCache.AddEntry(assm);

      return assm;
    }
  }

  internal class LoaderCache<T>
  {
    private readonly Dictionary<string, T> myCache = new Dictionary<string, T>();
    private readonly Func<string, T> myLoader;

    public LoaderCache(Func<string, T> loader)
    {
      myLoader = loader;
    }

    public T Load(string file)
    {
      T result;
      if (myCache.TryGetValue(file, out result)) return result;
      try
      {
        result = myLoader(file);
      }
      catch
      {
        result = default(T);
      }

      myCache[file] = result;
      return result;
    }    
  }
}