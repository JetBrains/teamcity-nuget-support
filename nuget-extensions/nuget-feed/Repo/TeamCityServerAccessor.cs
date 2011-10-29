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
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly string myRemoteUrl;

    public TeamCityServerAccessor(string remoteUrl)
    {
      myRemoteUrl = remoteUrl;
      LOG.Info("TeamCityServerAccessor created. TeamCity URL: " + myRemoteUrl);
    }

    public T ProcessRequest<T>(string urlSuffix, Func<HttpWebResponse, TextReader, T> result)
    {
      var requestUriString = myRemoteUrl.TrimEnd('/') + "/" + urlSuffix.TrimStart('/');
      LOG.Info("Requesting " + requestUriString);
      try
      {
        var wr = (HttpWebRequest) WebRequest.Create(requestUriString);
        wr.CachePolicy = new HttpRequestCachePolicy(HttpRequestCacheLevel.NoCacheNoStore);

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
        LOG.Info("Request " + requestUriString + " finished");
      }
    }

    public string TeamCityUrl
    {
      get { return myRemoteUrl; }
    }
  }
}