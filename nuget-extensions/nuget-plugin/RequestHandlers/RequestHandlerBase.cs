using System;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  internal abstract class RequestHandlerBase<TRequest, TResponse> : IRequestHandler where TResponse : class
  {
    private readonly JsonSerializerSettings _serializerSettings;

    protected RequestHandlerBase(ILogger logger)
    {
      Logger = logger;
      _serializerSettings = new JsonSerializerSettings {Formatting = Formatting.None};
      _serializerSettings.Converters.Add(new StringEnumConverter());
    }
    
    /// <summary>
    /// Gets the current <see cref="ILogger"/> to use for logging.
    /// </summary>
    protected ILogger Logger { get; }

    /// <summary>
    /// Gets a <see cref="CancellationToken"/> to use.
    /// </summary>
    public virtual CancellationToken CancellationToken { get; private set; } = CancellationToken.None;

    public async Task HandleResponseAsync(IConnection connection, Message message, IResponseHandler responseHandler,
      CancellationToken cancellationToken)
    {
      var timer = new Stopwatch();
      timer.Start();
      
      TRequest request = MessageUtilities.DeserializePayload<TRequest>(message);
      var requestType = message.Type.ToString().ToLower();
      Logger.Log(LogLevel.Verbose,
        $"Handling {requestType} '{message.Method}'. Time elapsed {timer.ElapsedMilliseconds}ms - Payload: {message.Payload.ToString(Formatting.None)}");
      try
      {
        TResponse response = null;
        try
        {
          response = await HandleRequestAsync(request).ConfigureAwait(false);
        }
        catch (Exception ex) when (cancellationToken.IsCancellationRequested)
        {
          Logger.Log(LogLevel.Verbose,
            $"Silently canceling the request handling due to a cancellation. Time elapsed: {timer.ElapsedMilliseconds}ms. Exception: {ex.InnerException}, Message: {ex.Message}");
          return;
        }

        // We don't want to print credentials on auth responses
        if (message.Method != MessageMethod.GetAuthenticationCredentials)
        {
          var logResponse = JsonConvert.SerializeObject(response, _serializerSettings);
          Logger.Log(LogLevel.Debug, string.Format("Sending response: {0}", logResponse));
        }

        Logger.Log(LogLevel.Verbose,
          $"Elapsed time before sending response. '{message.Type}' '{message.Method}': {timer.ElapsedMilliseconds}ms");
        // If we did not send a cancel message, we must submit the response even if cancellationToken is canceled.
        await responseHandler.SendResponseAsync(message, response, CancellationToken.None).ConfigureAwait(false);

        Logger.Log(LogLevel.Verbose,
          $"Elapsed time after sending response. '{message.Type}' '{message.Method}': {timer.ElapsedMilliseconds}ms");

        CancellationToken = CancellationToken.None;
      }
      catch (Exception ex)
      {
        Logger.Log(LogLevel.Verbose, $"Exception occured during handling '{message.Method}' '{message.RequestId}'. Time elapsed in ms: {timer.ElapsedMilliseconds}. Shutting down: {Program.IsShuttingDown}. Exception: {ex}");
        throw;
      }
      finally
      {
        Logger.Log(LogLevel.Verbose,
          $"Handling {requestType} '{message.Method}' done. Time elapsed {timer.ElapsedMilliseconds}ms");
      }
    }

    public abstract Task<TResponse> HandleRequestAsync(TRequest request);
  }
}
