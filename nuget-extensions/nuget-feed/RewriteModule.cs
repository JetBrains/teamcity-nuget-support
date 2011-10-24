using System.Text.RegularExpressions;
using System.Web;
using System.Web.Routing;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed
{
  public class RewriteModule : IHttpModule
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    public void Init(HttpApplication context)
    {
      context.BeginRequest += OnBeginRequest;
    }

    private void OnBeginRequest(object sender, System.EventArgs e)
    {
      var context = HttpContext.Current;
      var url = context.Request.RawUrl;
      url = Regex.Replace(url, "[\\/][\\/]+", "/");
      context.RewritePath(url);
    }

    public void Dispose()
    {
      
    }
  }
}