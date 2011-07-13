using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Linq;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetRunner
  {
    private readonly string myNuGetExe;
    private readonly Assembly myNuGetAssembly;
    private readonly List<EventHandler> myStartEvents = new List<EventHandler>();
    private readonly List<EventHandler> myFinishEvents = new List<EventHandler>();

    public NuGetRunner(string NuGetExe)
    {
      myNuGetExe = NuGetExe;
      if (!File.Exists(NuGetExe))
        throw new NuGetLoadException("Failed to find NuGet.exe at " + myNuGetExe);

      try
      {
        myNuGetAssembly = Assembly.LoadFrom(myNuGetExe);
      }
      catch (Exception e)
      {
        throw new NuGetLoadException("Failed to load NuGet assembly into AppDomain. " + e.Message, e);
      }

      NuGetExtensionsPath = new Lazy<string>(LocateNuGetExtensionsPath);
    }

    private string LocateNuGetExtensionsPath()
    {
      var mi = myNuGetAssembly.EntryPoint.DeclaringType;
      foreach (
        var type in
          new Func<Type>[] {() => mi.DeclaringType, () => myNuGetAssembly.GetType("NuGet.Program")}.Select(Compute).
            Where(x => x != null))
      {
        var field = type.GetField("ExtensionsDirectoryRoot",
                                  BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
        if (field != null && field.FieldType == typeof (string))
        {
          var extensionsPath = field.GetValue(null) as string;
          if (extensionsPath != null)
            return extensionsPath;
        }
      }
      //This is explicit path value taken from NuGet source code
      return Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "NuGet", "Commands");
    }

    public Lazy<string> NuGetExtensionsPath { get; private set; }

    public event EventHandler BeforeNuGetStarted { add { myStartEvents.Add(value); } remove { myStartEvents.Remove(value);  } }
    public event EventHandler AfterNuGetFinished { add { myFinishEvents.Add(value); } remove { myFinishEvents.Remove(value); } }

    private void CallEvents(IEnumerable<EventHandler> handler)
    {
      foreach (var h in handler)
      {
        try
        {
          h(this, EventArgs.Empty);
        } catch (Exception e)
        {
          Console.Error.WriteLine("Failed to execute event: " + e);
        }
      }
    }

    public int Run(string[] argz)
    {
      CallEvents(myStartEvents);

      try
      {
        var result = myNuGetAssembly.EntryPoint.Invoke(null, new[] {argz});

        if (result is int)
          return (int) result;

        return 0;
      } finally
      {
        CallEvents(myFinishEvents);
      }
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