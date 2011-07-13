using System.Collections.Generic;
using NuGet;
using NuGet.Commands;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.List", "Lists packages for given Id with parsable output")]
  public class NuGetTeamCityListCommand : Command
  {
    private readonly List<string> mySources = new List<string>();

    [Option("Sources to search for package", AltName = "s")]
    public ICollection<string> Source { get { return mySources; } }

    [Option("Package Id to check for version update")]
    public string Id { get; set; }

    [Option("NuGet Version Spec to constraint versions to be checked. Optional")]
    public string Version { get; set; }

    public override void ExecuteCommand()
    {
      System.Console.Out.WriteLine("TeamCity NuGet List command.");
      System.Console.Out.WriteLine("Sources: {0}", string.Join(", ", mySources.ToArray()));
      System.Console.Out.WriteLine("Package Id: {0}", Id ?? "<null>");
      System.Console.Out.WriteLine("Version: {0}", Version ?? "<null>");
    }
  }
}
