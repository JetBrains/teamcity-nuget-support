// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.Collections.Concurrent;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class Dispatcher : IResponseHandler, IDisposable
  {
    private readonly ConcurrentDictionary<string, InboundRequestContext> _inboundRequestContexts =
      new ConcurrentDictionary<string, InboundRequestContext>();

    private readonly InboundRequestProcessingHandler _inboundRequestProcessingHandler;

    private readonly ConcurrentDictionary<string, OutboundRequestContext> _outboundRequestContexts =
      new ConcurrentDictionary<string, OutboundRequestContext>();

    private readonly IRequestHandlers _requestHandlers;
    private Connection _connection;
    private bool _disposed;
    private int _nextRequestId;
    private readonly ILogger _logger;

    public Dispatcher(IRequestHandlers requestHandlers,
      InboundRequestProcessingHandler inboundRequestProcessingHandler,
      ILogger logger)
    {
      _requestHandlers = requestHandlers;
      _inboundRequestProcessingHandler = inboundRequestProcessingHandler;
      _logger = logger;
    }

    public void SetConnection(Connection connection)
    {
      _connection = connection;
    }

    public async Task<TInbound> SendRequestAndReceiveResponseAsync<TOutbound, TInbound>(
      MessageMethod method,
      TOutbound payload,
      CancellationToken cancellationToken)
      where TOutbound : class
      where TInbound : class
    {
      cancellationToken.ThrowIfCancellationRequested();

      var request = MessageUtilities.Create(CreateRequestId(), MessageType.Request, method, payload);
      var requestContext = new OutboundRequestContext(
        this,
        request,
        GetRequestTimeout(method),
        method != MessageMethod.Handshake,
        cancellationToken,
        _logger);

      _outboundRequestContexts[request.RequestId] = requestContext;
      var removeRequestContext = true;
      
      try
      {
        _logger.Log(LogLevel.Verbose, $"Sending request with ID {request.RequestId} and method {method}", false);
        await _connection.SendAsync(request, requestContext.CancellationToken).ConfigureAwait(false);

        _logger.Log(LogLevel.Verbose, $"Request with ID {request.RequestId} and method {method} sent.", false);
        var response = await requestContext.CompletionTask.ConfigureAwait(false);

        _logger.Log(LogLevel.Verbose, $"Response received for request with ID {request.RequestId} and method {method}", false);
        return MessageUtilities.DeserializePayload<TInbound>(response);
      }
      catch (OperationCanceledException) when (requestContext.CancellationToken.IsCancellationRequested)
      {
        _logger.Log(LogLevel.Verbose, $"Request with ID {request.RequestId} and method {method} was cancelled.", false);
        removeRequestContext = false;
        throw;
      }
      finally
      {
        if (removeRequestContext)
        {
          RemoveOutboundRequestContext(request.RequestId);
        }
      }
    }

    public Task SendResponseAsync<TPayload>(Message request, TPayload payload, CancellationToken cancellationToken)
      where TPayload : class
    {
      if (request == null)
      {
        throw new ArgumentNullException(nameof(request));
      }

      var response = MessageUtilities.Create(request.RequestId, MessageType.Response, request.Method, payload);
      return SendInboundTerminalMessageAsync(request, response, cancellationToken);
    }

    public void HandleIncomingMessage(Message message)
    {
      if (message == null)
      {
        throw new ProtocolException("Received an empty protocol message.");
      }

      _logger.Log(LogLevel.Verbose, $"Received message with ID {message.RequestId} and method {message.Method}", false);

      if (_outboundRequestContexts.TryGetValue(message.RequestId, out var outboundContext))
      {
        _logger.Log(LogLevel.Verbose,
          $"Handling response for request with ID {message.RequestId} and method {message.Method}",
          false);
        HandleOutboundResponse(outboundContext, message);
        return;
      }

      _logger.Log(LogLevel.Verbose,
        $"Handling inbound message with ID {message.RequestId} and method {message.Method}",
        false);
      switch (message.Type)
      {
        case MessageType.Cancel:
          HandleInboundCancel(message);
          break;

        case MessageType.Request:
          HandleInboundRequest(message);
          break;

        case MessageType.Fault:
          HandleInboundFault(message);
          break;

        default:
          throw new ProtocolException("Invalid message type: " + message.Type);
      }
    }

    public void Close()
    {
      _logger.Log(LogLevel.Verbose, "Closing dispatcher", false);

      _logger.Log(LogLevel.Verbose, "Cancelling outstanding requests", false);
      foreach (var context in _outboundRequestContexts.Values)
      {
        context.CancelWithoutProtocolMessage();
      }

      _logger.Log(LogLevel.Verbose, "Cancelling inbound requests", false);
      foreach (var context in _inboundRequestContexts.Values)
      {
        context.Cancel();
      }
    }

    public void Dispose()
    {
      if (_disposed)
      {
        return;
      }

      Close();
      _inboundRequestProcessingHandler.Dispose();
      _disposed = true;
    }

    public Task SendCancelResponseAsync(Message request)
    {
      var response = new Message(request.RequestId, MessageType.Cancel, request.Method);
      return SendInboundTerminalMessageAsync(request, response, CancellationToken.None);
    }

    public Task SendFaultResponseAsync(Message request, Exception exception)
    {
      var message = MessageUtilities.Create(request.RequestId, MessageType.Fault, request.Method,
        new Fault(exception.Message));
      return SendInboundTerminalMessageAsync(request, message, CancellationToken.None);
    }

    private void HandleOutboundResponse(OutboundRequestContext requestContext, Message message)
    {
      switch (message.Type)
      {
        case MessageType.Response:
          requestContext.HandleResponse(message);
          RemoveOutboundRequestContext(message.RequestId);
          break;

        case MessageType.Progress:
          requestContext.HandleProgress(message);
          break;

        case MessageType.Fault:
          requestContext.HandleFault(message);
          RemoveOutboundRequestContext(message.RequestId);
          break;

        case MessageType.Cancel:
          requestContext.HandleCancelResponse();
          RemoveOutboundRequestContext(message.RequestId);
          break;

        default:
          throw new ProtocolException("Invalid message type: " + message.Type);
      }
    }

    private void HandleInboundCancel(Message message)
    {
      if (_inboundRequestContexts.TryGetValue(message.RequestId, out var requestContext))
      {
        requestContext.Cancel();
      }
    }

    private static void HandleInboundFault(Message message)
    {
      var fault = MessageUtilities.DeserializePayload<Fault>(message);
      throw new ProtocolException(fault == null ? "Protocol fault." : fault.Message);
    }

    private void HandleInboundRequest(Message message)
    {
      if (_requestHandlers.TryGet(message.Method, out var handler))
      {
        var requestContext = new InboundRequestContext(this, _connection, message, handler.CancellationToken,
          _inboundRequestProcessingHandler, _logger);
        _inboundRequestContexts[message.RequestId] = requestContext;
        requestContext.BeginResponseAsync(handler);
        return;
      }

      var faultContext = new InboundRequestContext(this, _connection, message, CancellationToken.None,
        _inboundRequestProcessingHandler, _logger);
      faultContext.BeginFaultAsync(new ProtocolException("No request handler for " + message.Method));
    }

    private async Task SendInboundTerminalMessageAsync(Message request, Message message,
      CancellationToken cancellationToken)
    {
      try
      {
        _logger.Log(LogLevel.Verbose,
          $"Sending terminal message for request with ID {request.RequestId} and method {request.Method}",
          false);
        await _connection.SendAsync(message, cancellationToken).ConfigureAwait(false);
        _logger.Log(LogLevel.Verbose,
          $"Terminal message sent for request with ID {request.RequestId} and method {request.Method}",
          false);
      }
      finally
      {
        RemoveInboundRequestContext(request.RequestId);
      }
    }

    private void RemoveInboundRequestContext(string requestId)
    {
      if (_inboundRequestContexts.TryRemove(requestId, out var requestContext))
      {
        requestContext.Dispose();
      }
    }

    private void RemoveOutboundRequestContext(string requestId)
    {
      if (_outboundRequestContexts.TryRemove(requestId, out var requestContext))
      {
        requestContext.Dispose();
      }
    }

    private TimeSpan GetRequestTimeout(MessageMethod method)
    {
      return method == MessageMethod.Handshake
        ? _connection.Timeouts.HandshakeTimeout
        : _connection.Timeouts.RequestTimeout;
    }

    private string CreateRequestId()
    {
      return Interlocked.Increment(ref _nextRequestId).ToString(CultureInfo.InvariantCulture);
    }
  }
}
