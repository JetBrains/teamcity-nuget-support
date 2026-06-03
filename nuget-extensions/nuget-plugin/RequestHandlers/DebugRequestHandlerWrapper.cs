using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Connectivity;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  internal sealed class DebugRequestHandlerWrapper : IRequestHandler
  {
    private const string HandshakeDelayEnvironmentVariable = "TEAMCITY_NUGET_TEST_DELAY_HANDSHAKE_MS";
    private const string InitializeDelayEnvironmentVariable = "TEAMCITY_NUGET_TEST_DELAY_INITIALIZE_MS";
    private const string AuthenticationCredentialsDelayEnvironmentVariable =
      "TEAMCITY_NUGET_TEST_DELAY_GET_AUTHENTICATION_CREDENTIALS_MS";
    private const string RequestStartedMarkerEnvironmentVariable = "TEAMCITY_NUGET_TEST_NOTIFY_REQUEST_STARTED_FILE";
    private const string RequestCanceledMarkerEnvironmentVariable = "TEAMCITY_NUGET_TEST_NOTIFY_REQUEST_CANCELED_FILE";
    private const string RequestCompletedMarkerEnvironmentVariable = "TEAMCITY_NUGET_TEST_NOTIFY_REQUEST_COMPLETED_FILE";

    private readonly IRequestHandler _inner;
    private readonly MessageMethod _method;
    private readonly string _delayEnvironmentVariable;
    private readonly ILogger _logger;

    private DebugRequestHandlerWrapper(
      MessageMethod method,
      IRequestHandler inner,
      string delayEnvironmentVariable,
      ILogger logger)
    {
      _method = method;
      _inner = inner ?? throw new ArgumentNullException(nameof(inner));
      _delayEnvironmentVariable = delayEnvironmentVariable ?? throw new ArgumentNullException(nameof(delayEnvironmentVariable));
      _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public CancellationToken CancellationToken => _inner.CancellationToken;

    public static IRequestHandler Wrap(MessageMethod method, IRequestHandler handler, ILogger logger)
    {
      var delayEnvironmentVariable = GetDelayEnvironmentVariable(method);
      if (delayEnvironmentVariable == null)
      {
        return handler;
      }

      return new DebugRequestHandlerWrapper(method, handler, delayEnvironmentVariable, logger);
    }

    public async Task HandleResponseAsync(
      IConnection connection,
      Message message,
      IResponseHandler responseHandler,
      CancellationToken cancellationToken)
    {
      var delay = GetDelay();
      var isInstrumented = delay.HasValue;
      if (isInstrumented)
      {
        WriteMarker(RequestStartedMarkerEnvironmentVariable);
      }

      try
      {
        if (delay.HasValue)
        {
          _logger.Log(LogLevel.Verbose, $"Delaying {_method} response for test: {delay.Value}");
          await Task.Delay(delay.Value, cancellationToken).ConfigureAwait(false);
        }

        await _inner.HandleResponseAsync(connection, message, responseHandler, cancellationToken).ConfigureAwait(false);
        if (isInstrumented)
        {
          WriteMarker(RequestCompletedMarkerEnvironmentVariable);
        }
      }
      catch (OperationCanceledException) when (cancellationToken.IsCancellationRequested)
      {
        if (isInstrumented)
        {
          WriteMarker(RequestCanceledMarkerEnvironmentVariable);
        }

        throw;
      }
    }

    private static string GetDelayEnvironmentVariable(MessageMethod method)
    {
      switch (method)
      {
        case MessageMethod.Handshake:
          return HandshakeDelayEnvironmentVariable;

        case MessageMethod.Initialize:
          return InitializeDelayEnvironmentVariable;

        case MessageMethod.GetAuthenticationCredentials:
          return AuthenticationCredentialsDelayEnvironmentVariable;

        default:
          return null;
      }
    }

    private TimeSpan? GetDelay()
    {
      if (!int.TryParse(Environment.GetEnvironmentVariable(_delayEnvironmentVariable), out var delayMs) || delayMs <= 0)
      {
        return null;
      }

      return TimeSpan.FromMilliseconds(delayMs);
    }

    private void WriteMarker(string environmentVariable)
    {
      var path = Environment.GetEnvironmentVariable(environmentVariable);
      if (string.IsNullOrEmpty(path))
      {
        return;
      }

      File.WriteAllText(path, _method.ToString());
    }
  }
}
