using System;
using System.Web.Hosting;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed;
using WebActivator;
using log4net;

[assembly: PreApplicationStartMethod(typeof(Log4Net), "InitApplication")]
[assembly: ApplicationShutdownMethod(typeof(Log4Net), "ShutdownApplication")]

namespace JetBrains.TeamCity.NuGet.Feed
{
  public class Log4Net
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();
    private static readonly Log4netInitializer myInitializer = new Log4netInitializer();

    [UsedImplicitly]
    public static void ShutdownApplication()
    {
    }

    [UsedImplicitly]
    public static void InitApplication()
    {
      myInitializer.InitializeLogging(HostingEnvironment.MapPath("~/Log4Net.xml"), "teamcity-nuget-server");    
      LOG.Info("Starting NuGet Feed Server for TeamCity...");
      AppDomain.CurrentDomain.DomainUnload += delegate
                                                {
                                                  LOG.Info("NuGet application will be shut down");
                                                  myInitializer.ShutdownLogging();
                                                };
    }
  }
}