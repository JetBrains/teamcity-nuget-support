// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class Connection : IConnection
  {
    private readonly Dispatcher _dispatcher;
    private readonly ILogger _logger;
    private readonly TextReader _reader;
    private readonly object _writeLock = new object();
    private readonly TextWriter _writer;
    private Task _readLoopTask;
    private volatile ConnectionState _state = ConnectionState.ReadyToConnect;

    public Connection(TextReader reader, TextWriter writer, Dispatcher dispatcher, ILogger logger)
    {
      _reader = reader;
      _writer = writer;
      _dispatcher = dispatcher;
      _logger = logger;
      Timeouts = PluginTimeouts.Instance;
      _dispatcher.SetConnection(this);
    }

    public event EventHandler<ProtocolErrorEventArgs> Faulted;
    public event EventHandler RemoteClosed;

    public PluginTimeouts Timeouts { get; }

    public async Task ConnectAsync(CancellationToken cancellationToken)
    {
      _logger.Log(LogLevel.Verbose, "Connecting to NuGet.");
      
      cancellationToken.ThrowIfCancellationRequested();
      if (_state != ConnectionState.ReadyToConnect)
      {
        throw new InvalidOperationException("Connection has already been started.");
      }

      _state = ConnectionState.Connecting;
      
      _logger.Log(LogLevel.Verbose, "Starting read loop.");
      _readLoopTask = Task.Run(() => ReadLoopAsync(cancellationToken), cancellationToken);

      _state = ConnectionState.Handshaking;
      _logger.Log(LogLevel.Verbose, "Sending handshake request.");
      try
      {
        var response = await SendRequestAndReceiveResponseAsync<HandshakeRequest, HandshakeResponse>(
          MessageMethod.Handshake,
          new HandshakeRequest(PluginProtocolSession.CurrentProtocolVersion, PluginProtocolSession.MinimumProtocolVersion),
          cancellationToken).ConfigureAwait(false);
        
        _logger.Log(LogLevel.Verbose, "Handshake response received.");

        if (response == null || response.ResponseCode != MessageResponseCode.Success)
        {
          _state = ConnectionState.FailedToHandshake;
          throw new ProtocolException("Plugin handshake failed.");
        }

        _state = ConnectionState.Connected;
        
        _logger.Log(LogLevel.Verbose, "Connection established.");
      }
      catch(Exception ex)
      {
        _state = ConnectionState.FailedToHandshake;
        _logger.Log(LogLevel.Verbose, $"Handshake failed. Message: {ex.Message}");
        throw;
      }
    }

    public bool IsConnected => _state == ConnectionState.Connected;

    public Task<TInbound> SendRequestAndReceiveResponseAsync<TOutbound, TInbound>(
      MessageMethod method,
      TOutbound payload,
      CancellationToken cancellationToken)
      where TOutbound : class
      where TInbound : class
    {
      return _dispatcher.SendRequestAndReceiveResponseAsync<TOutbound, TInbound>(method, payload, cancellationToken);
    }

    public Task SendAsync(Message message, CancellationToken cancellationToken)
    {
      if (message == null)
      {
        throw new ArgumentNullException(nameof(message));
      }
      
      _logger.Log(LogLevel.Verbose,
        $"Sending message. Message type: {message.Type}, Method: {message.Method}, RequestId: {message.RequestId}",
        false);

      if (_state == ConnectionState.Closing || _state == ConnectionState.Closed)
      {
        return Task.FromResult(0);
      }

      if (_state < ConnectionState.Connecting)
      {
        throw new InvalidOperationException("Connection is not connected.");
      }

      cancellationToken.ThrowIfCancellationRequested();
      var line = MessageUtilities.SerializeMessage(message);
      
      lock (_writeLock)
      {
        _writer.WriteLine(line);
        _writer.Flush();
      }
      
      _logger.Log(LogLevel.Verbose, $"Message was sent. Message type: {message.Type}, Method: {message.Method}, RequestId: {message.RequestId}", false);

      return Task.FromResult(0);
    }

    public void Close()
    {
      _logger.Log(LogLevel.Verbose, "Closing connection.", false);
      if (_state == ConnectionState.Closed || _state == ConnectionState.Closing)
      {
        return;
      }

      _state = ConnectionState.Closing;
      _dispatcher.Close();
      _state = ConnectionState.Closed;
      
      _logger.Log(LogLevel.Verbose, "Connection closed.", false);
    }

    public void Dispose()
    {
      Close();
    }

    private async Task ReadLoopAsync(CancellationToken cancellationToken)
    {
      try
      {
        while (_state != ConnectionState.Closed && _state != ConnectionState.Closing && !cancellationToken.IsCancellationRequested)
        {
          var line = await _reader.ReadLineAsync().ConfigureAwait(false);
          if (line == null)
          {
            _logger.Log(LogLevel.Verbose, "Received null line from the reader.", false);
            
            RemoteClosed?.Invoke(this, EventArgs.Empty);
            return;
          }

          if (line.Length == 0)
          {
            _logger.Log(LogLevel.Verbose, "Received empty line from the reader.", false);
            continue;
          }

          _logger.Log(LogLevel.Verbose, $"Received message from reader.", false);
          var message = MessageUtilities.DeserializeMessage(line);
          _dispatcher.HandleIncomingMessage(message);
        }
      }
      catch (Exception e)
      {
        Faulted?.Invoke(this, new ProtocolErrorEventArgs(e, null));
      }
    }

    private enum ConnectionState
    {
      ReadyToConnect = 0,
      Connecting = 1,
      Handshaking = 2,
      Connected = 3,
      FailedToHandshake = 4,
      Closing = 5,
      Closed = 6
    }
  }
}
