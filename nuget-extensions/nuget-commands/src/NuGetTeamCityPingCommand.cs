using System.ComponentModel.Composition;
using System.Threading;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.Ping", "Command that dump NuGet and TeamCity extension versions. It is used to check NuGet<->TeamCity communications")]
  public class NuGetTeamCityPingCommand : CommandBase
  {
    [Option("Makes command wait for 1000ms before exit. Used for testing")]
    public bool Sleep { get; set; }

    [Import]
    public NuGetTeamCityInfo Info { get; set; }
    
    protected override void ExecuteCommandImpl()
    {
      Info.LogRuntimeInfo();
      if (Sleep)
      {
        Thread.Sleep(1000);
      }
    }
  }
}