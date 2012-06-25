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

      //Replace all NuGet sources with our sources.
      Sources.SavePackageSources(
        feeds.Feeds.SelectMany(
          def =>
          new[]
            {
              new PackageSource(def.Url + '/',
                                TeamCityAuthConstants.TeamCityFeedPrefix + Guid.NewGuid().ToString(), true),
              new PackageSource(def.Url + "/$metadata",
                                TeamCityAuthConstants.TeamCityFeedPrefix + Guid.NewGuid().ToString(), false),
            }
            .Select(x =>
              {
                x.UserName = def.UserName;
                x.Password = def.Password;
                return x;
              })
          )
          .ToArray()
        );
    }
  }
}
