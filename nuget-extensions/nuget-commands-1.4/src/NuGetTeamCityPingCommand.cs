using System.Threading;
using NuGet;
using NuGet.Commands;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.Ping", "Command that dump NuGet and TeamCity extension versions. It is used to check NuGet<->TeamCity communications")]
  public class NuGetTeamCityPingCommand : Command
  {
    [Option("Makes command wait for 1000ms before exit. Used for testing")]
    public bool Sleep { get; set; }

    public override void ExecuteCommand()
    {
      System.Console.Out.WriteLine("TeamCity NuGet Extension is available.");
      System.Console.Out.WriteLine("NuGet Version = {0}", typeof(Command).Assembly.GetName().Version);
      System.Console.Out.WriteLine("TeamCity Extension Version = {0}", GetType().Assembly.GetName().Version);
      
      if (Sleep)
      {
        Thread.Sleep(1000);
      }
    }
  }
}