using System;
using System.Collections.Generic;
using System.Linq;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.List", "Lists packages for given Id with parsable output")]
  public class NuGetTeamCityListCommand : ListCommandBase
  {
    [Option("NuGet package Source to search for package")]
    public string Source { get; set; }

    [Option("Package Id to check for version update")]
    public string Id { get; set; }

    [Option("NuGet Version Spec to constraint versions to be checked. Optional")]
    public string Version { get; set; }

    protected override void ExecuteCommandImpl()
    {
      if (string.IsNullOrWhiteSpace(Source))
        Source = NuGetConstants.DefaultFeedUrl;

      System.Console.Out.WriteLine("TeamCity NuGet List command.");
      System.Console.Out.WriteLine("Source: {0}", Source ?? "<null>");
      System.Console.Out.WriteLine("Package Id: {0}", Id ?? "<null>");
      System.Console.Out.WriteLine("Version: {0}", Version ?? "<null>");      
      System.Console.Out.WriteLine("Checking for latest version...");

      foreach (var p in GetPackages())
        PrintPackageInfo(p);
    }

    private IEnumerable<IPackage> GetPackages()
    {
      if (string.IsNullOrWhiteSpace(Source))
        throw new CommandLineException("-Source must be specified.");

      var allPackages = GetAllPackages(Source, new[] { Id });
      if (string.IsNullOrWhiteSpace(Version))
        return allPackages;

      return allPackages.Where(VersionUtility.ParseVersionSpec(Version).ToDelegate());
    }
  }
}
