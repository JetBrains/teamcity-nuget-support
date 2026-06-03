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
  internal sealed class OutboundRequestContext : IDisposable
  {
    private readonly CancellationTokenSource _cancellationTokenSource;
    private readonly Dispatcher _dispatcher;
    private readonly bool _isKeepAlive;
    private readonly Message _request;
    private readonly TaskCompletionSource<Message> _taskCompletionSource;
    private readonly TimeSpan? _timeout;
    private readonly Timer _timer;
    private int _isCancellationRequested;
    private bool _disposed;
    private readonly ILogger _logger;

    public OutboundRequestContext(
      Dispatcher dispatcher,
      Message request,
      TimeSpan? timeout,
      bool isKeepAlive,
      CancellationToken cancellationToken,
      ILogger logger)
    {
      _dispatcher = dispatcher;
      _request = request;
      _timeout = timeout;
      _isKeepAlive = isKeepAlive;
      _taskCompletionSource = new TaskCompletionSource<Message>(TaskCreationOptions.RunContinuationsAsynchronously);
      _cancellationTokenSource = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
      _logger = logger;
      CancellationToken = _cancellationTokenSource.Token;
      CancellationToken.Register(TryCancel);

      if (_timeout.HasValue)
      {
        _timer = new Timer(OnTimeout, null, _timeout.Value, Timeout.InfiniteTimeSpan);
      }
    }

    public CancellationToken CancellationToken { get; }
    public Task<Message> CompletionTask => _taskCompletionSource.Task;
    public string RequestId => _request.RequestId;

    public void HandleResponse(Message response)
    {
      _logger.Log(LogLevel.Verbose,$"Received response for request '{_request.RequestId}', method: {_request.Method}", false);
      _taskCompletionSource.TrySetResult(response);
    }

    public void HandleFault(Message fault)
    {
      _logger.Log(LogLevel.Verbose,$"Received fault for request '{_request.RequestId}', method: {_request.Method}", false);
      var payload = MessageUtilities.DeserializePayload<Fault>(fault);
      _taskCompletionSource.TrySetException(new ProtocolException(payload == null ? "Protocol fault." : payload.Message));
    }

    public void HandleProgress(Message progress)
    {
      _logger.Log(LogLevel.Verbose,$"Received progress for request '{_request.RequestId}', method: {_request.Method}", false);
      if (_timeout.HasValue && _isKeepAlive && _timer != null)
      {
        _timer.Change(_timeout.Value, Timeout.InfiniteTimeSpan);
      }
    }

    public void HandleCancelResponse()
    {
      _logger.Log(LogLevel.Verbose,$"Received cancel response for request '{_request.RequestId}', method: {_request.Method}", false);
      if (Interlocked.CompareExchange(ref _isCancellationRequested, 0, 0) == 0)
      {
        _taskCompletionSource.TrySetException(new ProtocolException("Invalid cancel response."));
        return;
      }

      _taskCompletionSource.TrySetCanceled();
    }

    public void CancelWithoutProtocolMessage()
    {
      _logger.Log(LogLevel.Verbose,$"Cancelling request '{_request.RequestId}', method: {_request.Method}", false);
      _taskCompletionSource.TrySetCanceled();
    }

    public void Dispose()
    {
      if (_disposed)
      {
        return;
      }

      _timer?.Dispose();

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

    private void OnTimeout(object state)
    {
      _logger.Log(LogLevel.Verbose,$"Request '{_request.RequestId}', method: {_request.Method} timed out.", false);
      TryCancel();
    }

    private void TryCancel()
    {
      if (!_taskCompletionSource.TrySetCanceled())
      {
        return;
      }

      if (Interlocked.CompareExchange(ref _isCancellationRequested, 1, 0) != 0)
      {
        return;
      }

      Task.Run(async () =>
               {
                 try
                 {
                   _logger.Log(LogLevel.Verbose,$"Sending cancel request for request '{_request.RequestId}', method: {_request.Method}", false);
                   await _dispatcher
                     .SendCancelResponseAsync(_request)
                     .ContinueWith(x => x.Exception, TaskContinuationOptions.OnlyOnFaulted)
                     .ConfigureAwait(false);
                   _logger.Log(LogLevel.Verbose,$"Cancel request sent for request '{_request.RequestId}', method: {_request.Method}", false);
                 }
                 catch
                 {
                   // ignored
                 }
               });
    }
  }
}
