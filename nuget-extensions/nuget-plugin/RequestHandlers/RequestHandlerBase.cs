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
      TRequest request = MessageUtilities.DeserializePayload<TRequest>(message);
      var requestType = message.Type.ToString().ToLower();
      Logger.Log(LogLevel.Debug, string.Format("Handling {0} '{1}': {2}", requestType, message.Method, message.Payload.ToString(Formatting.None)));
      
      TResponse response = await HandleRequestAsync(request).ConfigureAwait(false);
      
      // We don't want to print credentials on auth responses
      if (message.Method != MessageMethod.GetAuthenticationCredentials)
      {
        var logResponse = JsonConvert.SerializeObject(response, _serializerSettings);
        Logger.Log(LogLevel.Debug, string.Format("Sending response: {0}", logResponse));
      }

      await responseHandler.SendResponseAsync(message, response, cancellationToken).ConfigureAwait(false);
      CancellationToken = CancellationToken.None;
    }

    public abstract Task<TResponse> HandleRequestAsync(TRequest request);
  }
}
