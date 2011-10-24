using System;
using System.Net;
using System.Text;
using System.Web.Routing;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class NuGetPingHandler
  {
    private const string PING_HTTP_HEADER = "X-TeamCity-HostId";

    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    private readonly RequestContext myContext;
    private readonly ITeamCityServerAccessor myAccessor;

    public NuGetPingHandler(RequestContext context, ITeamCityServerAccessor accessor)
    {
      myContext = context;
      myAccessor = accessor;
    }


    public void ProcessRequest()
    {
      var ctx = myContext.HttpContext;
      var message = new StringBuilder("TeamCity server was not found at: " + myAccessor.TeamCityUrl + ". ");
      try
      {
        var result = myAccessor.ProcessRequest(
          "/packages-ping.html",
          (res, text) =>
            {
              if (res.StatusCode != HttpStatusCode.OK)
              {
                message.Append("Responce code: " + res.StatusCode + ". " + text.ReadToEnd());
                return null;
              }

              return res.Headers[PING_HTTP_HEADER];
            }
          );

        if (result != null)
        {
          ctx.Response.Headers[PING_HTTP_HEADER] = result;
          WriteStatus(HttpStatusCode.OK, "Hashcode matched");
          return;
        }

      } catch(Exception e)
      {
        message.Append("Failed to connect to TeamCity server. " + e);
        WriteStatus(HttpStatusCode.InternalServerError, message.ToString());
      }
            
      WriteStatus(HttpStatusCode.NotFound, message.ToString());
    }

    private  void WriteStatus(HttpStatusCode statusCode, string body = null)
    {
      myContext.HttpContext.Response.StatusCode = (int)statusCode;
      if (!String.IsNullOrEmpty(body))
      {
        myContext.HttpContext.Response.Write(body);
      }
    }
  }
}