// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class InboundRequestContext : IDisposable
  {
    private readonly CancellationTokenSource _cancellationTokenSource;
    private readonly IConnection _connection;
    private readonly Dispatcher _dispatcher;
    private readonly InboundRequestProcessingHandler _processingHandler;
    private readonly ILogger _logger;
    private bool _disposed;
    private readonly Message _request;
    private readonly CancellationToken _cancellationToken;

    public InboundRequestContext(
      Dispatcher dispatcher,
      IConnection connection,
      Message request,
      CancellationToken cancellationToken,
      InboundRequestProcessingHandler processingHandler,
      ILogger logger)
    {
      _dispatcher = dispatcher;
      _connection = connection;
      _request = request;
      _processingHandler = processingHandler;
      _logger = logger;
      _cancellationTokenSource = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
      _cancellationToken = _cancellationTokenSource.Token;
    }

    public void BeginResponseAsync(IRequestHandler requestHandler)
    {
      _logger.Log(LogLevel.Verbose, $"Processing inbound request for method '{_request.Method}', request ID: {_request.RequestId}", false);
      _processingHandler.Handle(_request.Method,
        () => ProcessResponseAsync(requestHandler),
        _cancellationToken);
    }

    public void BeginFaultAsync(Exception exception)
    {
      _logger.Log(LogLevel.Error, $"Processing inbound fault for method '{_request.Method}', request ID: {_request.RequestId}: {exception.Message}", false);
      _processingHandler.Handle(_request.Method,
        () => _dispatcher.SendFaultResponseAsync(_request,
          exception),
        CancellationToken.None);
    }

    public void Cancel()
    {
      _logger.Log(LogLevel.Verbose, $"Cancelling inbound request for method '{_request.Method}', request ID: {_request.RequestId}", false);
      try
      {
        _cancellationTokenSource.Cancel();
      }
      catch (ObjectDisposedException)
      {
      }
    }

    public void Dispose()
    {
      if (_disposed)
      {
        return;
      }

      try
      {
        _cancellationTokenSource.Cancel();
      }
      catch
      {
        // ignored
      }
      finally
      {
        _cancellationTokenSource.Dispose();
      }

      _disposed = true;
    }

    private async Task ProcessResponseAsync(IRequestHandler requestHandler)
    {
      try
      {
        _logger.Log(LogLevel.Verbose, $"Processing inbound response for method '{_request.Method}', request ID: {_request.RequestId}", false);
        await requestHandler.HandleResponseAsync(_connection, _request, _dispatcher, _cancellationToken)
          .ConfigureAwait(false);
        _logger.Log(LogLevel.Verbose, $"Inbound response processed for method '{_request.Method}', request ID: {_request.RequestId}", false);
      }
      catch (OperationCanceledException) when (_cancellationToken.IsCancellationRequested)
      {
        _logger.Log(LogLevel.Verbose, $"Inbound request for method '{_request.Method}', request ID: {_request.RequestId} was cancelled.", false);
        await _dispatcher.SendCancelResponseAsync(_request).ConfigureAwait(false);
      }
      catch (Exception ex)
      {
        _logger.Log(LogLevel.Error, $"Exception occured while processing inbound response for method '{_request.Method}', request ID: {_request.RequestId}: {ex.Message}", false);
        await _dispatcher.SendFaultResponseAsync(_request, ex).ConfigureAwait(false);
      }
    }
  }
}
