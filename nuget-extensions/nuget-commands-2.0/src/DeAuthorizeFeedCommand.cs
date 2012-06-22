using System.ComponentModel.Composition;
using NuGet;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.DeAuthorizeFeed", "Adds login/password into NuGet settings")]
  public class DeAuthorizeFeedCommand : CommandBase
  {
    [Import]
    public IPackageSourceProvider Sources { get; set; }

    protected override void ExecuteCommandImpl()
    {
      var allSources = Sources.LoadPackageSources().ToArray();
      Sources.SavePackageSources(allSources.Where(x => !x.Name.StartsWith(TeamCityAuthConstants.TeamCityFeedPrefix)));
    }
  }
}