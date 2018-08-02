using System.Threading;
using System.Threading.Tasks;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  internal abstract class RequestHandlerBase<TRequest, TResponse> : IRequestHandler where TResponse : class
  {
    protected RequestHandlerBase(PluginController plugin)
    {
      Plugin = plugin;
    }

    protected PluginController Plugin { get; }

    public CancellationToken CancellationToken { get; }

    public async Task HandleResponseAsync(IConnection connection, Message message, IResponseHandler responseHandler,
      CancellationToken cancellationToken)
    {
      TRequest request = MessageUtilities.DeserializePayload<TRequest>(message);
      TResponse response = await HandleRequestAsync(request).ConfigureAwait(false);

      await responseHandler.SendResponseAsync(message, response, cancellationToken).ConfigureAwait(false);
    }

    public abstract Task<TResponse> HandleRequestAsync(TRequest request);
  }
}
