// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using JetBrains.TeamCity.NuGet.Compatibility.Versioning;
using JetBrains.TeamCity.NuGet.Logging;
using JetBrains.TeamCity.NuGet.RequestHandlers;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class PluginProtocolSession : IDisposable
  {
    internal static readonly SemanticVersion CurrentProtocolVersion = new SemanticVersion(2, 0, 0);
    internal static readonly SemanticVersion MinimumProtocolVersion = new SemanticVersion(1, 0, 0);

    private readonly Connection _connection;
    private readonly ILogger _logger;
    private bool _closed;

    private PluginProtocolSession(Connection connection, ILogger logger)
    {
      _connection = connection;
      _logger = logger;
      _connection.RemoteClosed += OnRemoteClosed;
      _connection.Faulted += OnFaulted;
    }

    public event EventHandler<ProtocolErrorEventArgs> Faulted;
    public event EventHandler BeforeClose;
    public event EventHandler Closed;

    public IConnection Connection => _connection;

    public static async Task<PluginProtocolSession> CreateFromCurrentProcessAsync(IRequestHandlers requestHandlers,
      CancellationToken cancellationToken, ILogger logger)
    {
      LogPluginAssembly(logger);
      LogConnectionTimeouts(logger);
      LogThreadPool(logger);
      LogCurrentProcess(logger);
      
      logger.Log(LogLevel.Verbose, "Creating plugin protocol session.");

      var encoding = new UTF8Encoding(false);
      var reader = new StreamReader(Console.OpenStandardInput(), encoding);
      var writer = new StreamWriter(Console.OpenStandardOutput(), encoding);
      var dispatcher = new Dispatcher(
        requestHandlers,
        new InboundRequestProcessingHandler(new HashSet<MessageMethod> { MessageMethod.Handshake }, logger),
        logger);
      var connection = new Connection(reader, writer, dispatcher, logger);
      var session = new PluginProtocolSession(connection, logger);

      requestHandlers.TryAdd(
        MessageMethod.Handshake,
        DebugRequestHandlerWrapper.Wrap(MessageMethod.Handshake, new HandshakeRequestHandler(logger), logger));
      requestHandlers.TryAdd(MessageMethod.Close, new CloseRequestHandler(session, logger));
      requestHandlers.TryAdd(MessageMethod.MonitorNuGetProcessExit, new MonitorNuGetProcessExitRequestHandler(session, logger));

      logger.Log(LogLevel.Verbose, "Plugin protocol session created and connected to standard input/output streams.", false);
      await connection.ConnectAsync(cancellationToken).ConfigureAwait(false);

      logger.Log(LogLevel.Verbose, "Plugin protocol session connected.");
      return session;
    }

    private static void LogPluginAssembly(ILogger logger)
    {
      var assembly = typeof(PluginProtocolSession).GetTypeInfo().Assembly;
      var assemblyName = assembly.GetName();
      var informationalVersion = assembly.GetCustomAttributes<AssemblyInformationalVersionAttribute>()
        .Select(attribute => attribute.InformationalVersion)
        .FirstOrDefault();

      logger.Log(
        LogLevel.Verbose,
        $"Plugin assembly: name='{assemblyName.Name}', version='{assemblyName.Version}', informationalVersion='{informationalVersion}', fullName='{assembly.FullName}'.",
        false);
    }

    private static void LogConnectionTimeouts(ILogger logger)
    {
      var timeouts = PluginTimeouts.Instance;
      var idleTimeout = Environment.GetEnvironmentVariable("NUGET_PLUGIN_IDLE_TIMEOUT_IN_SECONDS") ?? "<not set>";
      logger.Log(
        LogLevel.Verbose,
        $"Connection timeouts: handshakeTimeout='{timeouts.HandshakeTimeout}', idleTimeoutEnvironmentValue='{idleTimeout}', requestTimeout='{timeouts.RequestTimeout}'.",
        false);
    }

    private static void LogThreadPool(ILogger logger)
    {
#if NETCOREAPP1_0
      logger.Log(LogLevel.Verbose, "ThreadPool parameters are unavailable on netcoreapp1.0.");
#else
      ThreadPool.GetMinThreads(out var minWorkerThreads, out var minCompletionPortThreads);
      ThreadPool.GetMaxThreads(out var maxWorkerThreads, out var maxCompletionPortThreads);
      ThreadPool.GetAvailableThreads(out var availableWorkerThreads, out var availableCompletionPortThreads);

      logger.Log(
        LogLevel.Verbose,
        $"ThreadPool: minWorkerThreads='{minWorkerThreads}', minCompletionPortThreads='{minCompletionPortThreads}', maxWorkerThreads='{maxWorkerThreads}', maxCompletionPortThreads='{maxCompletionPortThreads}', availableWorkerThreads='{availableWorkerThreads}', availableCompletionPortThreads='{availableCompletionPortThreads}'.",
        false);
#endif
    }

    private static void LogCurrentProcess(ILogger logger)
    {
      using (var process = Process.GetCurrentProcess())
      {
        logger.Log(
          LogLevel.Verbose,
          $"Current process: id='{process.Id}', processName='{process.ProcessName}', startTime='{GetProcessStartTime(process)}'.",
          false);
      }
    }

    private static string GetProcessStartTime(Process process)
    {
      try
      {
        return process.StartTime.ToString("O");
      }
      catch (Exception ex)
      {
        return $"<unavailable: {ex.Message}>";
      }
    }

    private void Close()
    {
      if (_closed)
      {
        return;
      }
      
      _logger.Log(LogLevel.Verbose, "Closing plugin protocol session.", false);

      _closed = true;
      BeforeClose?.Invoke(this, EventArgs.Empty);
      _connection.Close();
      Closed?.Invoke(this, EventArgs.Empty);
    }

    public void Dispose()
    {
      Close();
      _connection.RemoteClosed -= OnRemoteClosed;
      _connection.Faulted -= OnFaulted;
      _connection.Dispose();
    }

    private void OnRemoteClosed(object sender, EventArgs args)
    {
      Close();
    }

    private void OnFaulted(object sender, ProtocolErrorEventArgs args)
    {
      Faulted?.Invoke(this, args);
    }

    private sealed class CloseRequestHandler : IRequestHandler
    {
      private readonly PluginProtocolSession _session;
      private readonly ILogger _logger;

      public CloseRequestHandler(PluginProtocolSession session, ILogger logger)
      {
        _session = session;
        _logger = logger;
      }

      public CancellationToken CancellationToken => CancellationToken.None;

      public async Task HandleResponseAsync(IConnection connection, Message message, IResponseHandler responseHandler, CancellationToken cancellationToken)
      {
        _logger.Log(LogLevel.Verbose, "Handling close request", false);
        await responseHandler.SendResponseAsync(message, new CloseResponse(MessageResponseCode.Success), CancellationToken.None)
          .ConfigureAwait(false);
        _session.Close();
      }
    }

    private sealed class CloseResponse
    {
      public CloseResponse(MessageResponseCode responseCode)
      {
        ResponseCode = responseCode;
      }

      [JsonRequired]
      public MessageResponseCode ResponseCode { get; }
    }

    private sealed class HandshakeRequestHandler : IRequestHandler
    {
      private readonly ILogger _logger;
      public HandshakeRequestHandler(ILogger logger)
      {
        _logger = logger;
      }

      public CancellationToken CancellationToken => CancellationToken.None;

      public Task HandleResponseAsync(IConnection connection, Message message, IResponseHandler responseHandler, CancellationToken cancellationToken)
      {
        var request = MessageUtilities.DeserializePayload<HandshakeRequest>(message);
        var response = new HandshakeResponse(MessageResponseCode.Error, null);

        if (request != null &&
            request.MinimumProtocolVersion <= request.ProtocolVersion &&
            request.ProtocolVersion >= MinimumProtocolVersion &&
            request.MinimumProtocolVersion <= CurrentProtocolVersion)
        {
          var selectedVersion = CurrentProtocolVersion <= request.ProtocolVersion
            ? CurrentProtocolVersion
            : request.ProtocolVersion;
          response = new HandshakeResponse(MessageResponseCode.Success, selectedVersion);
        }
        
        _logger.Log(LogLevel.Verbose, $"Handshake: protocolVersion='{response.ProtocolVersion}'.");
        return responseHandler.SendResponseAsync(message, response, cancellationToken);
      }
    }

    private sealed class MonitorNuGetProcessExitRequestHandler : IRequestHandler
    {
      private readonly PluginProtocolSession _session;
      private readonly ILogger _logger;

      public MonitorNuGetProcessExitRequestHandler(PluginProtocolSession session, ILogger logger)
      {
        _session = session;
        _logger = logger;
      }

      public CancellationToken CancellationToken => CancellationToken.None;

      public Task HandleResponseAsync(IConnection connection, Message message, IResponseHandler responseHandler, CancellationToken cancellationToken)
      {
        var request = MessageUtilities.DeserializePayload<MonitorNuGetProcessExitRequest>(message);
        var responseCode = TryMonitorProcessExit(request?.ProcessId ?? 0)
          ? MessageResponseCode.Success
          : MessageResponseCode.NotFound;

        _logger.Log(LogLevel.Verbose, $"MonitorNuGetProcessExit: responseCode='{responseCode}'.");
        return responseHandler.SendResponseAsync(message, new MonitorNuGetProcessExitResponse(responseCode), cancellationToken);
      }

      private bool TryMonitorProcessExit(int processId)
      {
        if (processId <= 0)
        {
          return false;
        }

        _logger.Log(LogLevel.Verbose, $"MonitorNuGetProcessExit: processId='{processId}'.");
        try
        {
          var process = System.Diagnostics.Process.GetProcessById(processId);
          process.EnableRaisingEvents = true;
          process.Exited += (sender, args) => _session.Close();
          return true;
        }
        catch
        {
          _logger.Log(LogLevel.Verbose, $"MonitorNuGetProcessExit: processId='{processId}' not found.");
          return false;
        }
      }
    }
  }
}
