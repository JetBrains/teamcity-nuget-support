using System;
using System.ComponentModel.Composition;
using System.Linq;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.AuthorizeFeed", "Adds login/password into NuGet settings")]
  public class AuthorizeFeedCommand : CommandBase
  {
    [Option("Path to file containing feed credentials to update")]
    public string Request { get; set; }

    [Import]
    public IPackageSourceProvider Sources { get; set; }

    protected override void ExecuteCommandImpl()
    {
      var feeds = XmlSerialization.LoadRequests<AuthenticatedFeedsList>(Request);

      foreach (var feed in feeds.Feeds)
      {
        System.Console.Out.WriteLine("Fetched feed data for: {0}", feed.Url);
      }

      var packageSources = from def in feeds.Feeds
                           select new PackageSource(def.Url, TeamCityAuthConstants.TeamCityFeedPrefix + Guid.NewGuid().ToString(), true)
                             {
                               UserName = def.UserName,
                               Password = def.Password
                             };

      //Replace all NuGet sources with our sources.
      Sources.SavePackageSources(packageSources.ToArray());
    }
  }
}
