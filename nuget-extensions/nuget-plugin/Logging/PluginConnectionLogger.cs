// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Connectivity;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal class PluginConnectionLogger : LoggerBase
  {
    private readonly IConnection _connection;

    internal PluginConnectionLogger(IConnection connection)
    {
      _connection = connection;
    }

    protected override void WriteLog(DateTime logTimestamp, LogLevel logLevel, string logMessage, bool logNotifyNuGet)
    {
      // intentionally not awaiting here -- don't want to block forward progress just because we tried to log.
      if (logNotifyNuGet && _connection.IsConnected)
      {
        _connection
          .SendRequestAndReceiveResponseAsync<LogRequest, LogResponse>(
            MessageMethod.Log,
            new LogRequest(logLevel, $"    {logMessage}"),
            CancellationToken.None)
          .ContinueWith(x => x.Exception, TaskContinuationOptions.OnlyOnFaulted);
      }
    }
  }
}
