// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System.Collections.Generic;
using NuGet.Common;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal class MultiLogger : List<ILogger>, ILogger
  {
    private LogLevel? _minLogLevel;

    public void Log(LogLevel level, string message)
    {
      foreach (var logger in this)
      {
        logger.Log(level, message);
      }
    }

    public void SetLogLevel(LogLevel newLogLevel)
    {
      _minLogLevel = newLogLevel;

      foreach (var logger in this)
      {
        logger.SetLogLevel(newLogLevel);
      }
    }

    public new void Add(ILogger logger)
    {
      if (_minLogLevel.HasValue)
      {
        logger.SetLogLevel(_minLogLevel.Value);
      }

      base.Add(logger);
    }
  }
}
