using System.ComponentModel.Composition;
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

    private readonly NuGetTeamCityInfo myInfo;

    [ImportingConstructor]
    public NuGetTeamCityPingCommand(NuGetTeamCityInfo info)
    {
      myInfo = info;
      myInfo.LogRuntimeInfo();
    }

    protected override void ExecuteCommandImpl()
    {
      myInfo.LogRuntimeInfo();
      if (Sleep)
      {
        Thread.Sleep(1000);
      }
    }
  }
}