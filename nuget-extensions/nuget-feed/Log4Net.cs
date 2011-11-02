using System.Web.Hosting;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed;

[assembly: WebActivator.PreApplicationStartMethod(typeof (Log4Net), "InitApplication")]

namespace JetBrains.TeamCity.NuGet.Feed
{
  public class Log4Net
  {
    [UsedImplicitly]
    public static void InitApplication()
    {
      new Log4netInitializer().InitializeLogging(HostingEnvironment.MapPath("~/Log4Net.xml"), "teamcity-nuget-server");
    }
  }
}