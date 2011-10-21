using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Cache;
using System.Text;
using JetBrains.TeamCity.ServiceMessages.Read;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RemoteRepo : ITeamCityPackagesRepo
  {
    private readonly string myRemoteUrl;
    private readonly IServiceMessageParser myParser;
    private readonly PackageLoader myLoader;

    public RemoteRepo(string remoteUrl, IServiceMessageParser parser, PackageLoader loader)
    {
      myRemoteUrl = remoteUrl;
      myParser = parser;
      myLoader = loader;
    }

    public IEnumerable<TeamCityPackage> GetAllPackages()
    {
      var wr = (HttpWebRequest)WebRequest.Create(myRemoteUrl);
      wr.CachePolicy = new HttpRequestCachePolicy(HttpRequestCacheLevel.NoCacheNoStore);

      using (var webResponse = (HttpWebResponse)wr.GetResponse())
      {
        var stream = webResponse.GetResponseStream();
        if (stream == null)
          throw new Exception(string.Format("Failed to read packages from stream. Status code: {0}", webResponse.StatusCode));
        
        var streamReader = new StreamReader(stream, Encoding.UTF8);
        return myParser.ParseServiceMessages(streamReader).Select(myLoader.Load);
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