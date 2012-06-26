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
        Console.WriteLine("Fetched feed data for: {0}, User={1}, Password={2}", feed.Url, feed.UserName ?? "<null>", feed.Password == null ? "<null>" : "****");
      }

      Func<Credentials, string, bool, PackageSource> create =
        (def, url, enabled) => new PackageSource(url, TeamCityAuthConstants.TeamCityFeedPrefix + Guid.NewGuid().ToString(), enabled)
          {
            UserName = def.UserName,
            Password = def.Password
          };

    //Replace all NuGet sources with our sources.
      var feedsToAdd = feeds.Feeds.SelectMany(def => new[]
        {
          create(def, def.Url + '/', true),
          create(def, def.Url + "/$metadata", false),
        })
        .Union(
          feeds.Credentials.Select(def => create(def, def.Url, false))
        )
        .ToArray();

      Sources.SavePackageSources(feedsToAdd);
    }
  }
}
