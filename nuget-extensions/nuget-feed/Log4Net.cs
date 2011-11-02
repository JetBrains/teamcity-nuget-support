using System;
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
      const string key = "teamcity-dotnet-log-file";
      if (Environment.GetEnvironmentVariable(key) == null)
        Environment.SetEnvironmentVariable(key, Environment.CurrentDirectory);

      new Log4netInitializer().InitializeLogging(HostingEnvironment.MapPath("~/Log4Net.xml"));
    }
  }
}