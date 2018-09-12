// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System;
using System.Collections.Concurrent;
using System.Threading;
using NuGet.Common;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal abstract class LoggerBase : ILogger
  {
    private LogLevel _minLogLevel = LogLevel.Debug;
    private bool _allowLogWrites;

    private ConcurrentQueue<Tuple<LogLevel, string>> _bufferedLogs =
      new ConcurrentQueue<Tuple<LogLevel, string>>();

    public void Log(LogLevel level, string message)
    {
      if (!_allowLogWrites)
      {
        // cheap reserve, if it swaps out after we add, meh, we miss one log
        var buffer = _bufferedLogs;
        if (buffer != null)
        {
          buffer.Enqueue(Tuple.Create(level, message));
        }

        // we could pass this through if buffer is null since the Set message has already come through to unblock us, but
        // the race should be rare and we don't know exactly how nuget will behave with the parallelism so
        // opt to ignore this one racing log message.
        return;
      }

      if (_bufferedLogs != null)
      {
        ConcurrentQueue<Tuple<LogLevel, string>> buffer = null;
        buffer = Interlocked.CompareExchange(ref _bufferedLogs, null, _bufferedLogs);

        if (buffer != null)
        {
          foreach (var log in buffer)
          {
            if (log.Item1 >= _minLogLevel)
            {
              WriteLog(log.Item1, log.Item2);
            }
          }
        }
      }

      if (level >= _minLogLevel)
      {
        WriteLog(level, message);
      }
    }

    public void SetLogLevel(LogLevel newLogLevel)
    {
      _minLogLevel = newLogLevel;
      _allowLogWrites = true;
    }

    protected abstract void WriteLog(LogLevel logLevel, string message);
  }
}
