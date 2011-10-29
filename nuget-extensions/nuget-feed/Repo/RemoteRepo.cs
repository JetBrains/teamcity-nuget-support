using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using JetBrains.TeamCity.ServiceMessages.Read;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RemoteRepo : ITeamCityPackagesRepo
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly ITeamCityServerAccessor myRemote;
    private readonly IServiceMessageParser myParser;
    private readonly PackageLoader myLoader;

    public RemoteRepo(ITeamCityServerAccessor remote, IServiceMessageParser parser, PackageLoader loader)
    {
      myRemote = remote;
      myParser = parser;
      myLoader = loader;
    }

    public IEnumerable<TeamCityPackage> GetAllPackages()
    {
      try
      {
        return myRemote.ProcessRequest("/packages-metadata.html", ProcessPackages);
      } catch(Exception e)
      {
        LOG.Warn(string.Format("Failed to fetch all packages from TeamCity server. {0}", e.Message), e);
        return new TeamCityPackage[0];
      }
    }

    private IEnumerable<TeamCityPackage> ProcessPackages(HttpWebResponse response, TextReader reader)
    {
      if (response.StatusCode != HttpStatusCode.OK)
      {
        LOG.Warn("Failed to fetch packages. HTTP Status was: " + response.StatusCode);
        return new TeamCityPackage[0];
      }

      var list = myParser.ParseServiceMessages(reader).ToList();
      LOG.InfoFormat("Fetched {0} packages from TeamCity", list.Count);
      return list.Select(myLoader.Load).ToList();
    }

    public IEnumerable<TeamCityPackage> FilterById(IEnumerable<string> ids)
    {
      var set = new HashSet<string>(ids);
      if (set.Count == 0) return new TeamCityPackage[0];

      return GetAllPackages().Where(x=>set.Contains(x.Id));
    }

    public IEnumerable<TeamCityPackage> FiltetByIdLatest(IEnumerable<string> ids)
    {
      var set = new HashSet<string>(ids);
      if (set.Count == 0) return new TeamCityPackage[0];

      return GetAllPackages().Where(x=>x.IsLatestVersion).Where(x => set.Contains(x.Id));
    }
  }
}