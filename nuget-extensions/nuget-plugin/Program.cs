using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
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
                                  tokenSource.Cancel();
                                  eventArgs.Cancel = true;
                                };

      var semaphore = new SemaphoreSlim(0);

      try
      {
        return MainInternal(tokenSource, multiLogger, semaphore, args).GetAwaiter().GetResult();
      }
      catch (OperationCanceledException e)
      {
        // Multiple source restoration. Request will be cancelled if a package has been successfully restored from another source
        multiLogger.Log(LogLevel.Verbose, $"Request to credential provider was cancelled. Message: ${e.Message}");
        return 0;
      }
    }

    private static async Task<int> MainInternal(CancellationTokenSource tokenSource, MultiLogger multiLogger,
      SemaphoreSlim semaphore, string[] args)
    {
      Process.GetCurrentProcess().Dispose();

      var credentialProvider = new TeamCityCredentialProvider(multiLogger);
      var sdkInfo = new SdkInfo();
      var requestHandlers = new RequestHandlerCollection((method, handler) =>
                                                         {
                                                           if (method != MessageMethod.Close) return;

                                                           var plugin = GetPlugin(handler);

                                                           multiLogger.Add(new PluginConnectionLogger(plugin.Connection));

                                                           plugin.Connection.Faulted += (sender, a) =>
                                                                                        {
                                                                                          multiLogger.Log(
                                                                                            LogLevel.Error,
                                                                                            $"Faulted on message: {a.Message?.Type} {a.Message?.Method} {a.Message?.RequestId}");
                                                                                          multiLogger.Log(
                                                                                            LogLevel.Error,
                                                                                            a.Exception.ToString());
                                                                                        };

                                                           plugin.Closed += (sender, a) => semaphore.Release();
                                                         })
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
          using (IPlugin plugin = await PluginFactory
            .CreateFromCurrentProcessAsync(requestHandlers, ConnectionOptions.CreateDefault(), CancellationToken.None)
            .ConfigureAwait(false))
          {
            bool complete = await semaphore.WaitAsync(TimeSpan.FromDays(1), tokenSource.Token)
              .ConfigureAwait(continueOnCapturedContext: false);

            if (!complete)
            {
              multiLogger.Log(LogLevel.Error, "Timed out waiting for plug-in operations to complete");
            }
          }
        }
        catch (OperationCanceledException e)
        {
          // Multiple source restoration. Request will be cancelled if a package has been successfully restored from another source
          multiLogger.Log(LogLevel.Verbose, $"Request to credential provider was cancelled. Message: ${e.Message}");
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

    private static IPlugin GetPlugin(IRequestHandler handler)
    {
      if (!(handler is CloseRequestHandler closeRequestHandler))
      {
        throw new InvalidOperationException($"Expected CloseRequestHandler but was ${handler?.GetType()}");
      }

      var pluginField = closeRequestHandler
        .GetType()
        .GetField("_plugin", BindingFlags.NonPublic | BindingFlags.GetField | BindingFlags.Instance)
        ?.GetValue(handler);

      if (!(pluginField is IPlugin plugin))
      {
        throw new InvalidOperationException($"Expected IPlugin but was ${pluginField?.GetType()}");
      }

      return plugin;
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
