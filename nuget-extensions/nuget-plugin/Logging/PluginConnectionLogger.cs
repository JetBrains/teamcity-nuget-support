// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System.Threading;
using NuGet.Common;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal class PluginConnectionLogger : LoggerBase
  {
    private readonly IConnection _connection;

    internal PluginConnectionLogger(IConnection connection)
    {
      _connection = connection;
    }

    protected override void WriteLog(LogLevel logLevel, string message)
    {
      // intentionally not awaiting here -- don't want to block forward progress just because we tried to log.
      _connection.SendRequestAndReceiveResponseAsync<LogRequest, LogResponse>(
        MessageMethod.Log,
        new LogRequest(logLevel, $"    {message}"),
        CancellationToken.None);
    }
  }
}
