using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class Program
  {
    static int Main(string[] args)
    {
      try
      {
        return Main2(args);
      } catch(Exception e)
      {
        Console.Error.Write("NuGet Runner Failed");
        Console.Error.Write(e);
        return 2;
      }
    }

    static int Main2(string[] args)
    {
      Console.Out.WriteLine("JetBrains TeamCity NuGet Runner " + typeof(Program).Assembly.GetName().Version);      
      if (args.Length < 2) return Usage();

      string nuget = args[0];
      var runner = new NuGetRunner(nuget);
      ConfigureExtensions(runner);

      Console.Out.WriteLine("Starting NuGet.exe {1} from {0}", runner.NuGetAssembly.GetAssemblyPath(), runner.NuGetVersion);

      switch(args[1])
      {
        case "---TeamCity.DumpExtensionsPath":
          Console.Out.WriteLine("ExtensionsPath: {0}", runner.LocateNuGetExtensionsPath() ?? "null");
          return 0;
        case "--TeamCity.NuGetVersion":
          Console.Out.WriteLine("TeamCity.NuGetVersion: " + runner.NuGetVersion);
          Console.Out.WriteLine();

          if (args.Length >= 3)
          {
            string path = args[2];
            File.WriteAllText(path, runner.NuGetVersion.ToString());
          }

          return 0;
        
        default:
          return runner.Run(args.Skip(1).ToArray());
      }
    }

    private static void ConfigureExtensions(NuGetRunner runner)
    {
      if (runner.NuGetVersion.Major == 1 && runner.NuGetVersion.Minor <= 4 && runner.NuGetVersion.Revision < 20905)
      {
        Console.Out.WriteLine("Using shared plugin and mutex");
        new NuGetRunMutex(runner);
        new NuGetInstallExtensions4(runner, Extensions(runner));
        return;
      }

      new NuGetInstallExtensions5(runner, Extensions(runner));
    }

    private static IEnumerable<string> Extensions(NuGetRunner runner)
    {
      Func<string, string> path = p => Path.Combine(typeof(Program).GetAssemblyDirectory(), "plugins-" + p, "JetBrains.TeamCity.NuGet.ExtendedCommands." + p + ".dll");

      if (runner.NuGetVersion.Major >= 2 && runner.NuGetVersion.Minor >= 5)
      {
        yield return path("2.5");
      } else if (runner.NuGetVersion.Major >= 2)
      {
        yield return path("2.0");
      }
      else
      {
        yield return path("1.4");
      }
    }

    static int Usage()
    {
      Console.Out.WriteLine("JetBrains.TeamCity.NuGetRunner.exe <path to nuget> <nuget parameters>");
      return 1;
    }
  }
}
