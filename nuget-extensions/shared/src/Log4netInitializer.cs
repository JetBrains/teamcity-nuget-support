using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.CompilerServices;
using System.Xml;
using log4net;
using log4net.Config;

namespace JetBrains.TeamCity.NuGet.Feed
{
  [Serializable]
  public class Log4netInitializer 
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    public const string LOG_FILE_ENV_KEY = "teamcity-dotnet-log-file";
    public const string LOG_FOLDER_ENV_KEY = "teamcity-dotnet-log-folder";
    private const string LOG_CONFIG_REPLACE_CONSTANT = "teamcity-dotnet-log-file";

    private static void CurrentDomain_UnhandledException(object sender, UnhandledExceptionEventArgs e)
    {
      var exception = e.ExceptionObject as Exception;
      if (exception == null)
      {
        LOG.Warn("Unhandled (null) exception in current domain");
        return;
      }

      LOG.WarnFormat("Unhandled exception in current domain: {0}, {1}, {2}", exception, exception.StackTrace,
        exception.InnerException == null ? "(null)" : (object)exception.InnerException);
    }

    public void InitializeLogging(string logConfigFile, string defaultName)
    {
      LoadConfigFromFile(logConfigFile, defaultName);
      AppDomain.CurrentDomain.UnhandledException += CurrentDomain_UnhandledException;

      LOG.Info(Environment.NewLine + Environment.NewLine + Environment.NewLine + "===============================");
      LOG.InfoFormat("Started log4net from {0}", logConfigFile);
    }

    public void ShutdownLogging()
    {
      LOG.Info("Shutting down logging");
      LOG.Info(Environment.NewLine + Environment.NewLine + Environment.NewLine + "===============================");

      LogManager.Shutdown();
      LogManager.ResetConfiguration();
      AppDomain.CurrentDomain.UnhandledException -= CurrentDomain_UnhandledException;
    }

    private static void LoadConfigFromFile(string file, string defaultName)
    {
      var doc = new XmlDocument();

      string config = File.ReadAllText(file);

      var logFileName = GetLogFileName(defaultName);
      config = config.Replace("${" + LOG_CONFIG_REPLACE_CONSTANT + "}", logFileName);
      doc.LoadXml(config);

      XmlConfigurator.Configure(doc.DocumentElement);
    }

    private static string GetLogFileName(string defaultName)
    {
      var file = Environment.GetEnvironmentVariable(LOG_FILE_ENV_KEY);
      if (file != null) return file;


      string destPath = Environment.GetEnvironmentVariable(LOG_FOLDER_ENV_KEY)
                        ??
                        Path.Combine(Path.GetTempPath(), "TeamCity.NET");

      if (!Directory.Exists(destPath))
        Directory.CreateDirectory(destPath);

      string logFile = Path.Combine(destPath, string.Format("{1}-{0}.log", (DateTime.Now - new DateTime(2011, 02, 05, 16, 22, 10)).Ticks, defaultName));

      string newLogFile = logFile;
      int i = 1;
      while (File.Exists(newLogFile))
      {
        newLogFile = logFile + "." + ++i;
      }

      return newLogFile;
    }
  }


  public static class LogManagerHelper {
    [MethodImpl(MethodImplOptions.NoInlining)]
    public static ILog GetCurrentClassLogger()
    {
      return LogManager.GetLogger(new StackFrame(1, false).GetMethod().DeclaringType);
    }
  }

}