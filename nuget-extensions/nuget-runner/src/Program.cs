using System;
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
      Console.Out.WriteLine("Starting NuGet with additional commands");
      if (args.Length < 2) return Usage();

      string nuget = args[0];
      var runner = new NuGetRunner(nuget);
      ConfigureExtensions(runner);

      switch(args[1])
      {
        case "---TeamCity.DumpExtensionsPath":
          Console.Out.WriteLine("ExtensionsPath: {0}", runner.NuGetExtensionsPath.Value);
          return 0;
        
        default:
          return runner.Run(args.Skip(1).ToArray());
      }
    }

    private static void ConfigureExtensions(NuGetRunner runner)
    {
      new NuGetRunMutex(runner);
      new NuGetInstallExtensions(runner,
                                 new[]
                                   {
                                     Path.Combine(runner.GetType().GetAssemblyDirectory(),
                                                  "JetBrains.TeamCity.NuGet.ExtendedCommands.dll")
                                   });
    }

    static int Usage()
    {
      Console.Out.WriteLine("JetBrains.TeamCity.NuGetRunner.exe <path to nuget> <nuget parameters>");
      return 1;
    }
  }
}
