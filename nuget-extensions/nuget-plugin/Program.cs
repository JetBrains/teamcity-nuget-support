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
    public static async Task<int> Main(string[] args)
    {
      DebugBreakIfPluginDebuggingIsEnabled();

      var tokenSource = new CancellationTokenSource();
      var multiLogger = new MultiLogger();

      var fileLogger = GetFileLogger();
      if (fileLogger != null)
      {
        multiLogger.Add(fileLogger);
      }

      Console.CancelKeyPress += (sender, eventArgs) =>
                                {
                                  tokenSource.Cancel();
                                  eventArgs.Cancel = true;
                                };

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

        using (IPlugin plugin = await PluginFactory
          .CreateFromCurrentProcessAsync(requestHandlers, ConnectionOptions.CreateDefault(), CancellationToken.None)
          .ConfigureAwait(continueOnCapturedContext: false))
        {
          multiLogger.Add(new PluginConnectionLogger(plugin.Connection));
          await RunNuGetPluginsAsync(plugin, multiLogger, tokenSource.Token).ConfigureAwait(continueOnCapturedContext: false);
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

    private static async Task RunNuGetPluginsAsync(IPlugin plugin, ILogger logger, CancellationToken cancellationToken)
    {
      SemaphoreSlim semaphore = new SemaphoreSlim(0);

      plugin.Connection.Faulted += (sender, a) =>
                                   {
                                     logger.Log(LogLevel.Error, $"Faulted on message: {a.Message?.Type} {a.Message?.Method} {a.Message?.RequestId}");
                                     logger.Log(LogLevel.Error, a.Exception.ToString());
                                   };

      plugin.Closed += (sender, a) => semaphore.Release();

      bool complete = await semaphore.WaitAsync(TimeSpan.FromDays(1), cancellationToken)
        .ConfigureAwait(continueOnCapturedContext: false);

      if (!complete)
      {
        logger.Log(LogLevel.Error, "Timed out waiting for plug-in operations to complete");
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
