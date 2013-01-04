using System.Threading;
using NuGet;
using NuGet.Commands;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.Ping", "Command that dump NuGet and TeamCity extension versions. It is used to check NuGet<->TeamCity communications")]
  public class NuGetTeamCityPingCommand : CommandBase
  {
    [Option("Makes command wait for 1000ms before exit. Used for testing")]
    public bool Sleep { get; set; }

    public NuGetTeamCityPingCommand()
    {
      LogRuntimeInfo();
    }

    private void LogRuntimeInfo()
    {
      System.Console.Out.WriteLine("TeamCity NuGet Extension is available.");
      System.Console.Out.WriteLine("NuGet Version = {0}", typeof (Command).Assembly.GetName().Version);
      System.Console.Out.WriteLine("TeamCity Extension = {0}", GetType().Assembly.GetName().FullName);
    }

    protected override void ExecuteCommandImpl()
    {
      LogRuntimeInfo();
      if (Sleep)
      {
        Thread.Sleep(1000);
      }
    }
  }
}