// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System;
using System.IO;
using NuGet.Common;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal class FileLogger : LoggerBase
  {
    private readonly string _filePath;

    internal FileLogger(string filePath)
    {
      _filePath = filePath;
    }

    protected override void WriteLog(LogLevel logLevel, string message)
    {
      var dateString = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss,fff");
      var logLevelString = logLevel.ToString().ToUpper();
      File.AppendAllText(_filePath, $"[{dateString}]   {logLevelString} - {message}\n");
    }
  }
}
