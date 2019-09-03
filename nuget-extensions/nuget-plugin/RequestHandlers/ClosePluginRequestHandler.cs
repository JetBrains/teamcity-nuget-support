using System;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Annotations;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  internal class ClosePluginRequestHandler : IRequestHandler
  {
    private readonly IRequestHandler _closeRequestHandler;
    public CancellationToken CancellationToken => CancellationToken.None;

    public ClosePluginRequestHandler([NotNull] IRequestHandler closeRequestHandler)
    {
      _closeRequestHandler = closeRequestHandler ?? throw new ArgumentNullException(nameof(closeRequestHandler));
    }

    public async Task HandleResponseAsync([NotNull] IConnection connection, [NotNull] Message request,
      [NotNull] IResponseHandler responseHandler,
      CancellationToken cancellationToken)
    {
      if (connection == null) throw new ArgumentNullException(nameof(connection));
      if (request == null) throw new ArgumentNullException(nameof(request));
      if (responseHandler == null) throw new ArgumentNullException(nameof(responseHandler));

      cancellationToken.ThrowIfCancellationRequested();

      await _closeRequestHandler.HandleResponseAsync(connection, request, responseHandler, cancellationToken);


    }
  }
}
