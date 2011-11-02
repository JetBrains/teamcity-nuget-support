using System;
using System.IO;
using System.Net;
using System.Net.Cache;
using System.Text;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class TeamCityServerAccessor : ITeamCityServerAccessor
  {
    private readonly RepositoryPaths myPaths;
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();


    public TeamCityServerAccessor(RepositoryPaths paths)
    {
      myPaths = paths;    
      LOG.InfoFormat("TeamCityServerAccessor created. TeamCity URL: {0}, token: {1}", myPaths.TeamCityBaseUri, paths.Token);
    }

    public T ProcessRequest<T>(string urlSuffix, Func<HttpWebResponse, TextReader, T> result)
    {
      var requestUriString = TeamCityUrl.TrimEnd('/') + "/" + urlSuffix.TrimStart('/');
      LOG.Debug("Requesting " + requestUriString);
      try
      {
        var wr = (HttpWebRequest) WebRequest.Create(requestUriString);
        wr.CachePolicy = new HttpRequestCachePolicy(HttpRequestCacheLevel.NoCacheNoStore);
        wr.Headers.Add("X-TeamCity-Auth", myPaths.Token);

        using (var webResponse = (HttpWebResponse) wr.GetResponse())
        {
          var stream = webResponse.GetResponseStream();
          if (stream == null)
            throw new Exception(string.Format("Failed to read packages from stream. Status code: {0}", webResponse.StatusCode));

          var streamReader = new StreamReader(stream, Encoding.UTF8);
          return result(webResponse, streamReader);
        }        
      }
      catch (Exception e)
      {        
        LOG.Warn("Request to " + requestUriString + ". Failed. " + e.Message, e);        
        throw new Exception(string.Format("Failed to fetch all packages from: {0}. {1}", requestUriString, e.Message), e);
      } finally
      {
        LOG.Debug("Request " + requestUriString + " finished");
      }
    }

    public string TeamCityUrl
    {
      get { return myPaths.TeamCityBaseUri; }
    }
  }
}