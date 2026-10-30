// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System.Collections.Concurrent;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal class MultiLogger : ILogger
  {
    private readonly ConcurrentBag<ILogger> _loggers = new ConcurrentBag<ILogger>();
    private LogLevel? _minLogLevel;
    
    public void Log(LogLevel level, string message, bool notifyNuGet = true)
    {
      foreach (var logger in _loggers)
      {
        logger.Log(level, message, notifyNuGet);
      }
    }

    public void SetLogLevel(LogLevel newLogLevel)
    {
      _minLogLevel = newLogLevel;

      foreach (var logger in _loggers)
      {
        logger.SetLogLevel(newLogLevel);
      }
    }

    public void Add(ILogger logger)
    {
      if (_minLogLevel.HasValue)
      {
        logger.SetLogLevel(_minLogLevel.Value);
      }

      _loggers.Add(logger);
    }
  }
}
