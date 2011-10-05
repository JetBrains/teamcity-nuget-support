using System;
using System.IO;
using System.Net;
using CassiniDev;
using JetBrains.TeamCity.NuGetRunner;

namespace JetBrains.TeamCity.NuGet.Server
{
  public static class NuGetServerHost
  {
    static int Main(string[] _args)
    {
      Console.Out.WriteLine("JetBrains TeamCity NuGet Feed Server " + typeof(NuGetServerHost).Assembly.GetName().Version);
      Console.Out.WriteLine("Usage: ");
      Console.Out.WriteLine(" /port:XXX       sets port number to bind");
      Console.Out.WriteLine(" /hostname:XXX   sets port number to bind");
      Console.Out.WriteLine("");

      var argz = new Args(_args);
      try
      {
        return RunServer(argz);
      } catch(Exception e)
      {
        Console.Error.WriteLine("Application failed with unexpected error: " + e);
        return 1;
      }
    }

    private static int RunServer(Args argz)
    {
      var home = typeof (NuGetServerHost).Assembly.GetAssemblyDirectory();
      var webApp = Path.Combine(home, "feed");

      Console.Out.WriteLine("Starting web server from: {0}", webApp);
      if (!Directory.Exists(webApp))
      {
        Console.Error.WriteLine("Directory does not exist: {0}", webApp);
        return 1;
      }
      var port = argz.GetInt("port", 8411);
      var hostName = argz.Get("hostName", "localhost:" + port);

      var server = new CassiniDev.Server(port, "/", webApp, IPAddress.Loopback, hostName, 10000);
      server.Start();
      server.RequestComplete += (s, o) => Console.Out.WriteLine("Request {0} Completed.", o.RequestLog.PathTranslated);
      server.Start();

      return 0;
    }
  }

  
}
