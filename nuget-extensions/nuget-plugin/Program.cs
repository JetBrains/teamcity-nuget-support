using System;
using System.Diagnostics;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.RequestHandlers;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet
{
  internal static class Program
  {
    private static readonly TraceSource Logger = new TraceSource("CredentialPlugin");

    public static async Task<int> Main(string[] args)
    {
      DebugBreakIfPluginDebuggingIsEnabled();

      var tokenSource = new CancellationTokenSource();

      Console.CancelKeyPress += (sender, eventArgs) =>
                                {
                                  tokenSource.Cancel();
                                  eventArgs.Cancel = true;
                                };

      var credentialProvider = new TeamCityCredentialProvider(Logger);
      var requestHandlers = new RequestHandlerCollection
                            {
                              {
                                MessageMethod.GetAuthenticationCredentials,
                                new GetAuthenticationCredentialsRequestHandler(Logger, credentialProvider)
                              },
                              {
                                MessageMethod.GetOperationClaims,
                                new GetOperationClaimsRequestHandler(Logger)
                              },
                              {
                                MessageMethod.Initialize, 
                                new InitializeRequestHandler(Logger)
                              },
                            };

      if (String.Equals(args.SingleOrDefault(), "-plugin", StringComparison.OrdinalIgnoreCase))
      {
        using (IPlugin plugin = await PluginFactory
          .CreateFromCurrentProcessAsync(requestHandlers, ConnectionOptions.CreateDefault(), CancellationToken.None)
          .ConfigureAwait(continueOnCapturedContext: false))
        {
          await RunNuGetPluginsAsync(plugin, Logger, tokenSource.Token)
            .ConfigureAwait(continueOnCapturedContext: false);
        }

        return 0;
      }

      if (requestHandlers.TryGet(MessageMethod.GetAuthenticationCredentials, out IRequestHandler requestHandler) &&
          requestHandler is GetAuthenticationCredentialsRequestHandler getAuthenticationCredentialsRequestHandler)
      {
        var request = new GetAuthenticationCredentialsRequest(new Uri(args[0]), isRetry: false, isNonInteractive: true);
        var response = await getAuthenticationCredentialsRequestHandler
          .HandleRequestAsync(request).ConfigureAwait(continueOnCapturedContext: false);

        Console.WriteLine(response?.Username);
        Console.WriteLine(response?.Password);
        Console.WriteLine(response?.Password.ToJsonWebTokenString());

        return 0;
      }

      return -1;
    }

    private static async Task RunNuGetPluginsAsync(IPlugin plugin, TraceSource traceSource, CancellationToken cancellationToken)
    {
      SemaphoreSlim semaphore = new SemaphoreSlim(0);

      plugin.Connection.Faulted += (sender, a) =>
                                   {
                                     traceSource.Error(
                                       $"Faulted on message: {a.Message?.Type} {a.Message?.Method} {a.Message?.RequestId}");
                                     traceSource.Error(a.Exception.ToString());
                                   };

      plugin.Closed += (sender, a) => semaphore.Release();

      bool complete = await semaphore.WaitAsync(TimeSpan.FromMinutes(1), cancellationToken)
        .ConfigureAwait(continueOnCapturedContext: false);

      if (!complete)
      {
        Logger.Error("Timed out waiting for plug-in operations to complete");
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
  }
}
