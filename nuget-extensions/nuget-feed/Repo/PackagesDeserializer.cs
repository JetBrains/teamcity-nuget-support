using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using JetBrains.TeamCity.ServiceMessages.Read;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackagesDeserializer
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();
    private readonly IServiceMessageParser myParser;
    private readonly PackageLoader myLoader;

    public PackagesDeserializer(IServiceMessageParser parser, PackageLoader loader)
    {
      myParser = parser;
      myLoader = loader;
    }

    public IEnumerable<TeamCityPackage> ProcessPackages(HttpWebResponse response, TextReader reader)
    {
      if (response.StatusCode != HttpStatusCode.OK)
      {
        LOG.Warn("Failed to fetch packages. HTTP Status was: " + response.StatusCode);
        return new TeamCityPackage[0];
      }

      return ProcessPackages(reader);
    }

    public IEnumerable<TeamCityPackage> ProcessPackages(TextReader reader)
    {
      var list = myParser.ParseServiceMessages(reader).ToList();
      LOG.InfoFormat("Fetched {0} packages from TeamCity", list.Count);
      return list.Select(myLoader.Load).ToList();
    }
  }
}