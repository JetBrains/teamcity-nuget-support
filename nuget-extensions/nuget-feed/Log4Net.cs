using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed;

[assembly: log4net.Config.XmlConfigurator(ConfigFile = "Web.config", Watch = true)]
[assembly: WebActivator.PreApplicationStartMethod(typeof (Log4Net), "InitApplication")]

namespace JetBrains.TeamCity.NuGet.Feed
{
  public class Log4Net
  {
    [UsedImplicitly]
    public static void InitApplication()
    {
      log4net.Config.XmlConfigurator.Configure();
    }
  }
}