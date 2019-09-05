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
    private readonly Guid _id = Guid.NewGuid();
    private readonly string _filePath;

    internal FileLogger(string filePath)
    {
      _filePath = filePath;
    }

    protected override void WriteLog(LogLevel logLevel, string message)
    {
      var dateString = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss,fff");
      var logLevelString = logLevel.ToString().ToUpper();

      using(var fileStream = new FileStream(_filePath, FileMode.OpenOrCreate, FileAccess.Write, FileShare.ReadWrite))
      using (var stream = new StreamWriter(fileStream))
      {
        fileStream.Seek(0, SeekOrigin.End);
        stream.Write($"[{dateString}][{_id}]   {logLevelString} - {message}\n");
        fileStream.Flush();
      }
    }
  }
}
