using System;
using System.Linq;
using System.Reflection;

namespace nuget_runner
{
  public class Program
  {
    static int Main(string[] args)
    {
      Console.Out.WriteLine("JetBrains TeamCity NuGet Runner " + typeof(Program).Assembly.GetName().Version);
      Console.Out.WriteLine("Starting NuGet with additional commands");
      if (args.Length < 2) return Usage();

      string nuget = args[0];
      string[] nugetArgs = args.Skip(1).ToArray();

      var nugetAssembly = Assembly.LoadFrom(nuget);

      var result = nugetAssembly.EntryPoint.Invoke(null, new[] {nugetArgs});

      if (result is int)
        return (int) result;

      return 0;
    }

    static int Usage()
    {
      Console.Out.WriteLine("JetBrains.TeamCity.NuGetRunner.exe <path to nuget> <nuget parameters>");
      return 1;
    }
  }
}
