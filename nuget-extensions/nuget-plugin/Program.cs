using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Logging;
using JetBrains.TeamCity.NuGet.RequestHandlers;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet
{
  internal static class Program
  {
    private static bool ourShuttingDown;
    public static bool IsShuttingDown => Volatile.Read(ref ourShuttingDown);

    public static int Main(string[] args)
    {
      DebugBreakIfPluginDebuggingIsEnabled();

      var tokenSource = new CancellationTokenSource();
      var multiLogger = new MultiLogger();

      var fileLogger = GetFileLogger();
      if (fileLogger != null)
      {
        multiLogger.Add(fileLogger);
      }

      multiLogger.Log(LogLevel.Verbose, "Entered nuget credentials plugin");

      Console.CancelKeyPress += (sender, eventArgs) =>
                                {
                                  multiLogger.Log(LogLevel.Verbose, "Received cancel signal.");
                                  tokenSource.Cancel();
                                  eventArgs.Cancel = true;
                                };

      try
      {
        return MainInternal(tokenSource, multiLogger, args).GetAwaiter().GetResult();
      }
      catch (OperationCanceledException e)
      {
        // Multiple source restoration. Request will be cancelled if a package has been successfully restored from another source
        multiLogger.Log(LogLevel.Verbose, $"Request to credential provider was cancelled. Message: {e.Message}");
        return 0;
      }
      catch (Exception e)
      {
        multiLogger.Log(LogLevel.Verbose, $"Request to credential provider failed. Message: {e.Message}");
        return -1;
      }
    }

    private static async Task<int> MainInternal(CancellationTokenSource tokenSource, MultiLogger multiLogger, string[] args)
    {
      var credentialProvider = new TeamCityCredentialProvider(multiLogger);
      var sdkInfo = new SdkInfo();
      var requestHandlers = new RequestHandlerCollection
                            {
                              {
                                MessageMethod.GetAuthenticationCredentials,
                                new GetAuthenticationCredentialsRequestHandler(multiLogger, credentialProvider)
                              },
                              {
                                MessageMethod.GetOperationClaims,
                                new GetOperationClaimsRequestHandler(multiLogger, credentialProvider, sdkInfo)
                              },
                              {
                                MessageMethod.SetLogLevel,
                                new SetLogLevelHandler(multiLogger)
                              },
                              {
                                MessageMethod.Initialize,
                                new InitializeRequestHandler(multiLogger)
                              },
                              {
                                MessageMethod.SetCredentials,
                                new SetCredentialsRequestHandler(multiLogger)
                              }
                            };

      if (String.Equals(args.SingleOrDefault(), "-plugin", StringComparison.OrdinalIgnoreCase))
      {
        multiLogger.Log(LogLevel.Verbose, "Running in plug-in mode");

        try
        {
          using (var plugin = await PluginFactory
            .CreateFromCurrentProcessAsync(requestHandlers, ConnectionOptions.CreateDefault(), tokenSource.Token)
            .ConfigureAwait(continueOnCapturedContext: false))
          {
            multiLogger.Add(new PluginConnectionLogger(plugin.Connection));
            multiLogger.Log(LogLevel.Verbose, "Plugin connected");

            var shutdownTimeout = GetShutdownTimeout();
            await WaitForPluginExitAsync(plugin, multiLogger, shutdownTimeout).ConfigureAwait(continueOnCapturedContext: false);
          }
        }
        catch (OperationCanceledException e)
        {
          // Multiple source restoration. Request will be cancelled if a package has been successfully restored from another source
          multiLogger.Log(LogLevel.Verbose, $"Request to credential provider was cancelled. Message: {e.Message}");
        }

        return 0;
      }

      if (requestHandlers.TryGet(MessageMethod.GetAuthenticationCredentials, out IRequestHandler requestHandler) &&
          requestHandler is GetAuthenticationCredentialsRequestHandler getAuthenticationCredentialsRequestHandler)
      {
        multiLogger.Log(LogLevel.Verbose, "Running in stand-alone mode");

        if (args.Length == 0)
        {
          Console.WriteLine("Usage: CredentialProvider.TeamCity.exe <NuGetFeedUrl>");
          return 1;
        }

        var request = new GetAuthenticationCredentialsRequest(
          uri: new Uri(args[0]),
          isRetry: false,
          isNonInteractive: true,
          canShowDialog: false
        );
        var response = getAuthenticationCredentialsRequestHandler.HandleRequestAsync(request).GetAwaiter().GetResult();

        Console.WriteLine(response?.Username);
        Console.WriteLine(response?.Password);
        Console.WriteLine(response?.Password.ToJsonWebTokenString());

        return 0;
      }

      return -1;
    }

    private static TimeSpan GetShutdownTimeout()
    {
      const int defaultTimeoutSeconds = 120;
      var timeoutStr = Environment.GetEnvironmentVariable("NUGET_PLUGIN_SHUTDOWN_TIMEOUT_IN_SECONDS");

      return int.TryParse(timeoutStr, out var parsedTimeout)
        ? TimeSpan.FromSeconds(parsedTimeout)
        : TimeSpan.FromSeconds(defaultTimeoutSeconds);
    }

    private static async Task WaitForPluginExitAsync(IPlugin plugin, ILogger logger, TimeSpan shutdownTimeout)
    {
      logger.Log(LogLevel.Verbose, "Subscribing on events");

      var beginShutdownTaskSource = new TaskCompletionSource<object>();
      var endShutdownTaskSource = new TaskCompletionSource<object>();

      plugin.Connection.Faulted += (sender, a) =>
                                   {
                                     logger.Log(LogLevel.Error,
                                       $"Faulted on message: {a.Message?.Type} {a.Message?.Method} {a.Message?.RequestId}");
                                     logger.Log(LogLevel.Error, a.Exception.ToString());
                                   };

      plugin.BeforeClose += (sender, args) =>
                            {
                              logger.Log(LogLevel.Verbose, "Handling BeforeCLose event");

                              Volatile.Write(ref ourShuttingDown, true);
                              beginShutdownTaskSource.TrySetResult(null);
                            };

      plugin.Closed += (sender, a) =>
                       {
                         logger.Log(LogLevel.Verbose, "Handling Closed event");

                         beginShutdownTaskSource.TrySetResult(null);
                         endShutdownTaskSource.TrySetResult(null);
                       };

      logger.Log(LogLevel.Verbose, $"Waiting for plugin exit. Shutdown timeout: {shutdownTimeout}");

      await beginShutdownTaskSource.Task;
      
      logger.Log(LogLevel.Verbose, $"Begin shutdown completed.");

      var completedTask = await Task.WhenAny(endShutdownTaskSource.Task, Task.Delay(shutdownTimeout));
      if (completedTask != endShutdownTaskSource.Task)
      {
        logger.Log(LogLevel.Error, "Timed out waiting for plug-in operations to complete");
      }
      else
      {
        logger.Log(LogLevel.Verbose, "Plugin operations completed");
      }
    }

    private static void DebugBreakIfPluginDebuggingIsEnabled()
    {
      if (!string.IsNullOrEmpty(Environment.GetEnvironmentVariable("NUGET_PLUGIN_DEBUG")))
      {
        while (!Debugger.IsAttached)
        {
          Thread.Sleep(100);
        }
      }
    }
    
    private static FileLogger GetFileLogger()
    {
      var location = Environment.GetEnvironmentVariable("NUGET_PLUGIN_LOG_PATH");
      if (string.IsNullOrEmpty(location))
      {
        return null;
      }

      Directory.CreateDirectory(Path.GetDirectoryName(location));
      var fileLogger = new FileLogger(location);
      fileLogger.SetLogLevel(LogLevel.Verbose);

      return fileLogger;
    }
  }
}
