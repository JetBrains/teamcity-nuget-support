using System;
using System.IO;
using System.Net;
using System.Threading;
using CassiniDev;
using JetBrains.TeamCity.NuGetRunner;

namespace JetBrains.TeamCity.NuGet.Server
{
  public static class NuGetServerHost
  {
    static int Main(string[] _args)
    {
      Console.Out.WriteLine("JetBrains TeamCity NuGet Feed Server " + typeof(NuGetServerHost).Assembly.GetName().Version);    
      var argz = new Args(_args);
      if (argz.Contains("help"))
      {
        Usage();
        return 0;
      }

      try
      {
        return RunServer(argz);
      } catch(Exception e)
      {
        Console.Error.WriteLine("Application failed with unexpected error: " + e);
        return 1;
      }
    }

    private static void Usage()
    {
      Console.Out.WriteLine("Usage: ");
      Console.Out.WriteLine(" /port:XXX       sets port number to bind");
      Console.Out.WriteLine(" /hostname:XXX   sets port number to bind");
      Console.Out.WriteLine("");
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

      //TODO: Add code to check if server is still alive.
      var server = new CassiniDev.Server(port, "/", webApp, IPAddress.Loopback, hostName, 10000, false, true);
      server.RequestComplete += (s, o) => Console.Out.WriteLine("Request {0} : {1}", o.RequestLog.PathTranslated, o.ResponseLog.StatusCode);
      
      try
      {
        server.Start();
      } catch(Exception e)
      {
        Console.Error.WriteLine("Failed to start CassiniDev server: " + e);
        return 1;
      }

      Console.Out.WriteLine("Server is running on http://localhost:{0}", port);
      return Hung();      
    }

    private static int Hung()
    {
      while (true)
      {
        //TODO: check exit signal
        try
        {
          Thread.Sleep(TimeSpan.FromMinutes(10));
        }
        catch
        {
          return 0;
        }
      }
    }
  }

  
}
