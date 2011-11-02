using System.Web.Hosting;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed;
using log4net;

[assembly: WebActivator.PreApplicationStartMethod(typeof (Log4Net), "InitApplication")]

namespace JetBrains.TeamCity.NuGet.Feed
{
  public class Log4Net
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();


    [UsedImplicitly]
    public static void InitApplication()
    {
      new Log4netInitializer().InitializeLogging(HostingEnvironment.MapPath("~/Log4Net.xml"), "teamcity-nuget-server");    
      LOG.Info("Starting NuGet Feed Server for TeamCity...");
    }
  }
}