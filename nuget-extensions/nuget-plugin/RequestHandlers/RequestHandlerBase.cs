using System;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  internal abstract class RequestHandlerBase<TRequest, TResponse> : IRequestHandler where TResponse : class
  {
    protected RequestHandlerBase(TraceSource logger)
    {
      Logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public TraceSource Logger { get; }

    public CancellationToken CancellationToken { get; }

    public Task HandleResponseAsync(IConnection connection, Message message, IResponseHandler responseHandler,
      CancellationToken cancellationToken)
    {
      TRequest request = MessageUtilities.DeserializePayload<TRequest>(message);
      TResponse response = HandleRequest(request);

      return responseHandler.SendResponseAsync(message, response, cancellationToken);
    }

    public abstract TResponse HandleRequest(TRequest request);
  }
}
