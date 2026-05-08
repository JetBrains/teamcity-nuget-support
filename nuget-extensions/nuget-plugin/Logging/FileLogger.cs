// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System;
using System.IO;
using System.Threading;
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
        stream.Write($"[{dateString}][{_id}][{FormatCurrentThreadName()}] {logLevelString} - {message}\n");
        fileStream.Flush();
      }
    }

    private static string FormatCurrentThreadName()
    {
      var name = Thread.CurrentThread.Name + "." + Thread.CurrentThread.ManagedThreadId;
      
      const int maxLength = 20;

      if (name.Length == maxLength)
      {
        return name;
      }
      
      if (name.Length < maxLength)
      {
        return name.PadLeft(maxLength);
      }

      const string separator = "..";
      var startLength = (maxLength - separator.Length) / 2;
      var endLength = maxLength - separator.Length - startLength;
      return name.Substring(0, startLength) + separator + name.Substring(name.Length - endLength);
    }
  }
}
