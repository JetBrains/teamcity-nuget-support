// Copyright (c) Microsoft. All rights reserved.
//
// Licensed under the MIT license.

using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;

namespace JetBrains.TeamCity.NuGet.Logging
{
  internal class FileLogger : LoggerBase, IDisposable
  {
    private const int BUFFER_SIZE_THRESHOLD = 10;
    private const int FLUSH_INTERVAL_MS = 200;
    
    private readonly Guid _id = Guid.NewGuid();
    private readonly object _writeLock = new object();
    private readonly List<string> _buffer = new List<string>();
    private readonly FileStream _fileStream;
    private readonly StreamWriter _writer;
    private readonly Timer _flushTimer;
    private bool _disposed;

    internal FileLogger(string filePath)
    {
      var newFilePath = GenerateFilePathWithTimestamp(filePath);
      _fileStream = new FileStream(newFilePath, FileMode.OpenOrCreate, FileAccess.Write, FileShare.ReadWrite);
      _fileStream.Seek(0, SeekOrigin.End);
      _writer = new StreamWriter(_fileStream) { AutoFlush = false };
      _flushTimer = new Timer(Flush, null, TimeSpan.FromMilliseconds(FLUSH_INTERVAL_MS), TimeSpan.FromMilliseconds(FLUSH_INTERVAL_MS));
    }

    protected override void WriteLog(DateTime logTimestamp, LogLevel logLevel, string logMessage, bool logNotifyNuGet)
    {
      var dateString = logTimestamp.ToString("yyyy-MM-dd HH:mm:ss,fff");
      var logLevelString = logLevel.ToString().ToUpper();
      var logLine = $"[{dateString}][{_id}][{FormatCurrentThreadName()}] {logLevelString} - {logMessage}";

      lock (_writeLock)
      {
        if (_disposed)
        {
          return;
        }

        _buffer.Add(logLine);
        if (_buffer.Count > BUFFER_SIZE_THRESHOLD)
        {
          FlushUnsafe();
        }
      }
    }

    public void Dispose()
    {
      _flushTimer.Dispose();
      lock (_writeLock)
      {
        if (_disposed)
        {
          return;
        }

        FlushUnsafe();
        _disposed = true;
        _writer.Dispose();
        _fileStream.Dispose();
      }
    }

    private void Flush(object state)
    {
      lock (_writeLock)
      {
        if (_disposed)
        {
          return;
        }

        FlushUnsafe();
      }
    }

    private void FlushUnsafe()
    {
      if (_buffer.Count == 0)
      {
        return;
      }

      foreach (var logLine in _buffer)
      {
        _writer.WriteLine(logLine);
      }

      _buffer.Clear();
      _writer.Flush();
    }

    private static string GenerateFilePathWithTimestamp(string filePath)
    {
      var directory = Path.GetDirectoryName(filePath);
      var fileName = Path.GetFileNameWithoutExtension(filePath);
      var extension = Path.GetExtension(filePath);
      var encodedSuffix = Convert.ToString(DateTime.Now.Ticks, 16);
      var newFileName = $"{fileName}_{encodedSuffix}{extension}";
      var newFilePath = string.IsNullOrEmpty(directory) ? newFileName : Path.Combine(directory, newFileName);
      return newFilePath;
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
