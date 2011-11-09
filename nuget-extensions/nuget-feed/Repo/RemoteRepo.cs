using System;
using System.Collections.Generic;
using System.Linq;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RemoteRepo : ITeamCityPackagesRepo
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly ITeamCityServerAccessor myRemote;
    private readonly PackagesDeserializer myDeserializer;

    public RemoteRepo(ITeamCityServerAccessor remote, PackagesDeserializer deserializer)
    {
      myRemote = remote;
      myDeserializer = deserializer;
    }

    public IEnumerable<TeamCityPackage> GetAllPackages()
    {
      try
      {
        return myRemote.ProcessRequest("/packages-metadata.html", myDeserializer.ProcessPackages);
      } catch(Exception e)
      {
        LOG.Warn(string.Format("Failed to fetch all packages from TeamCity server. {0}", e.Message), e);
        return new TeamCityPackage[0];
      }
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