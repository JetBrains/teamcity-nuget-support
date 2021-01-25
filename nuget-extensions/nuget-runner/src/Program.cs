using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class Program
  {
    private static readonly Version NuGet35Version = new Version(3, 5);
    private static readonly string NugetPackagesPath = "NUGET_PACKAGES";

    static int Main(string[] args)
    {
      try
      {
        return Main2(args);
      }
      catch (Exception e)
      {
        Console.Error.Write("NuGet Runner Failed");
        Console.Error.Write(e);
        return 2;
      }
    }

    static int Main2(string[] args)
    {
      Console.Out.WriteLine("JetBrains TeamCity NuGet Runner " + typeof (Program).Assembly.GetName().Version);
      if (args.Length < 2) return Usage();

      string nuget = args[0];
      var runner = new NuGetRunner(nuget);
      ConfigureExtensions(runner);
      CheckEnvironment(runner);

      Console.Out.WriteLine("Starting NuGet.exe {1} from {0}", runner.NuGetAssembly.GetAssemblyPath(),
        runner.NuGetVersion);

      switch (args[1])
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
      Func<string, string> path = p => Path.Combine(typeof (Program).GetAssemblyDirectory(), "plugins-" + p, "JetBrains.TeamCity.NuGet.ExtendedCommands." + p + ".dll");

      if (runner.NuGetVersion.Major >= 5 && runner.NuGetVersion.Minor >= 8)
      {
        yield return path("5.8");
      }
      else if (runner.NuGetVersion.Major >= 4 && runner.NuGetVersion.Minor >= 0)
      {
        yield return path("4.0");
      }
      else if (runner.NuGetVersion.Major >= 3 && runner.NuGetVersion.Minor >= 5)
      {
        yield return path("3.5");
      }
      else if (runner.NuGetVersion.Major >= 3 && runner.NuGetVersion.Minor >= 3)
      {
        yield return path("3.3");
      }
      else if (runner.NuGetVersion.Major >= 3 && runner.NuGetVersion.Minor >= 2)
      {
        yield return path("3.2");
      }
      else if (runner.NuGetVersion.Major >= 2 && runner.NuGetVersion.Minor >= 8 && runner.NuGetVersion.Build >= 60717) // from 2.8.6
      {
        yield return path("2.8.6");
      }
      else if (runner.NuGetVersion.Major >= 2 && runner.NuGetVersion.Minor >= 8) // to 2.8.5
      {
        yield return path("2.8");
      }
      else if (runner.NuGetVersion.Major >= 2 && runner.NuGetVersion.Minor >= 5)
      {
        yield return path("2.5");
      }
      else if (runner.NuGetVersion.Major >= 2)
      {
        yield return path("2.0");
      }
      else
      {
        yield return path("1.4");
      }
    }

    private static void CheckEnvironment(NuGetRunner runner)
    {
      // Workaround for issue https://github.com/NuGet/Home/issues/4277
      if (runner.NuGetVersion < NuGet35Version || !IsSystemAccount())
      {
        return;
      }

      var variables = Environment.GetEnvironmentVariables();
      if (variables.Contains(NugetPackagesPath))
      {
        return;
      }

      var tempDirectory = Path.GetTempPath();
      if (string.IsNullOrEmpty(tempDirectory))
      {
        return;
      }

      var packagesPath = Path.Combine(tempDirectory, ".nuget", "packages");

      Console.Out.WriteLine("Setting '{0}' environment variable to '{1}'", NugetPackagesPath, packagesPath);
      runner.AddEnvironmentVariable(NugetPackagesPath, packagesPath);

      // Set nuget packages path for subsequent MSBuild launch
      Console.Out.WriteLine("##teamcity[setParameter name='env.{0}' value='{1}']", NugetPackagesPath, packagesPath);
    }

    private static bool IsSystemAccount()
    {
      using (var identity = System.Security.Principal.WindowsIdentity.GetCurrent())
      {
        return identity.IsSystem;
      }
    }

    static int Usage()
    {
      Console.Out.WriteLine("JetBrains.TeamCity.NuGetRunner.exe <path to nuget> <nuget parameters>");
      return 1;
    }
  }
}
