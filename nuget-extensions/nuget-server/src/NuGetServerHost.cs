using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading;
using System.Xml;
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
        Thread.CurrentThread.CurrentCulture = CultureInfo.InvariantCulture;
        Thread.CurrentThread.CurrentUICulture = CultureInfo.InvariantCulture;

        AppDomain.CurrentDomain.UnhandledException +=
          (s, o) => Console.Out.WriteLine("Unhandled Exception: " + (o.ExceptionObject ?? "null"));

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
      Console.Out.WriteLine(" /port:XXX                         sets port number to bind");
      Console.Out.WriteLine(" /PackagesSpecUri:XXX         ");
      Console.Out.WriteLine(" /Logs:XXX         ");
      Console.Out.WriteLine("");
    }

    private static void PatchWebConfig(string appHome, Dictionary<string, string> parameters)
    {
      var doc = new XmlDocument();
      var configFile = Path.Combine(appHome, "Web.config");
      doc.Load(configFile);

      var settingsElement = (XmlElement)doc.SelectSingleNode("configuration/appSettings");
      if (settingsElement == null)
        throw new Exception("Failed to patch Web.config");

      foreach (var e in parameters)
      {
        var node = (XmlElement)settingsElement.SelectSingleNode("add[@key='" + e.Key + "']");
        if (node != null)
        {
          node.SetAttribute("value", e.Value);
        } else
        {
          node = (XmlElement) settingsElement.AppendChild(doc.CreateElement("add"));
          node.SetAttribute("key", e.Key);
          node.SetAttribute("value", e.Value);
        }
      }

      doc.Save(configFile);
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

      var webContextParameters = new Dictionary<string, string>
                                   {
                                     {"PackagesSpecUri", argz.Get("PackagesSpecUri", "")},                                     
                                   };
      if (webContextParameters.Values.Any(String.IsNullOrWhiteSpace))
      {
        Console.Out.WriteLine("Not All parameters are specified. ");
        Usage();
        return 1;
      }
      Environment.SetEnvironmentVariable("teamcity-dotnet-log-file", argz.Get("Logs", Environment.CurrentDirectory));

      PatchWebConfig(webApp, webContextParameters);

      //TODO: Add code to check if server is still alive.
      var server = new CassiniDev.Server(port, "/", webApp, false, true);
      
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
