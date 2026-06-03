// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System;
using System.Collections.Concurrent;
using System.Threading;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal abstract class LoggerBase : ILogger
  {
    private LogLevel _minLogLevel = LogLevel.Debug;
    private bool _allowLogWrites;

    private ConcurrentQueue<LogItem> _bufferedLogs =
      new ConcurrentQueue<LogItem>();

    public void Log(LogLevel level, string message, bool notifyNuGet = true)
    {
      if (!_allowLogWrites)
      {
        // cheap reserve, if it swaps out after we add, meh, we miss one log
        var buffer = _bufferedLogs;
        if (buffer != null)
        {
          buffer.Enqueue(LogItem.Create(DateTime.Now, level, message, notifyNuGet));
        }

        // we could pass this through if buffer is null since the Set message has already come through to unblock us, but
        // the race should be rare and we don't know exactly how nuget will behave with the parallelism so
        // opt to ignore this one racing log message.
        return;
      }

      if (_bufferedLogs != null)
      {
        var buffer = Interlocked.CompareExchange(ref _bufferedLogs, null, _bufferedLogs);

        if (buffer != null)
        {
          foreach (var log in buffer)
          {
            if (log.Level >= _minLogLevel)
            {
              WriteLog(log.Timestamp, log.Level, log.Message, log.NotifyNuGet);
            }
          }
        }
      }

      if (level >= _minLogLevel)
      {
        WriteLog(DateTime.Now, level, message, notifyNuGet);
      }
    }

    public void SetLogLevel(LogLevel newLogLevel)
    {
      _minLogLevel = newLogLevel;
      _allowLogWrites = true;
    }

    protected abstract void WriteLog(DateTime logTimestamp, LogLevel logLevel, string logMessage, bool logNotifyNuGet);
    
    private class LogItem
    {
      public DateTime Timestamp { get; }
      public LogLevel Level { get; }
      public string Message { get; }
      public bool NotifyNuGet { get; }
      
      private LogItem(DateTime timestamp, LogLevel level, string message, bool notifyNuGet)
      {
        Timestamp = timestamp;
        Level = level;
        Message = message;
        NotifyNuGet = notifyNuGet;
      }
      
      public static LogItem Create(DateTime timestamp, LogLevel level, string message, bool notifyNuGet)
      {
        return new LogItem(timestamp, level, message, notifyNuGet);
      }
    }
  }
}
