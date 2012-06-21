using System.ComponentModel.Composition;
using System.Linq;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.DumpFeeds", "Dumps all configured NuGet feeds")]
  public class DumpFeeds : CommandBase
  {
    [Option("Path to write available feeds")]
    public string Response { get; set; }

    [Import]
    public IPackageSourceProvider Sources { get; set; }

    protected override void ExecuteCommandImpl()
    {
      var feeds = (from src in Sources.LoadPackageSources()
                   where src.IsEnabled
                   select new AuthenticatedFeed
                     {
                       Url = src.Source,
                       UserName = src.UserName,
                       Password = src.Password
                     }).ToArray();

      foreach (var feed in feeds)
      {
        System.Console.Out.WriteLine("Fetched feed data for: {0}", feed.Url);
      }

      XmlSerialization.SaveRequests(
        new AuthenticatedFeedsList
          {
            Feeds = feeds
          },
        Response);
    }
  }
}