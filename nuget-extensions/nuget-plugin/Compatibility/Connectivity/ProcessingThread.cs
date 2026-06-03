// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.Collections.Concurrent;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class ProcessingThread : IDisposable
  {
    private readonly TimeSpan _pollingDelay;
    private readonly ILogger _logger;
    private readonly ConcurrentQueue<ProcessingTask> _tasks = new ConcurrentQueue<ProcessingTask>();
    private long _nextTaskId;
    private volatile bool _closed;
    private bool _disposed;
    private Task _processingTask;

    public ProcessingThread(TimeSpan pollingDelay, ILogger logger)
    {
      _pollingDelay = pollingDelay;
      _logger = logger;
    }

    public void Start()
    {
      if (_processingTask != null)
      {
        throw new InvalidOperationException("The processing thread is already started.");
      }

      _logger.Log(LogLevel.Verbose, $"Starting processing thread with polling delay: {_pollingDelay}", false);

      _processingTask = Task.Factory.StartNew(
        ProcessAsync,
        CancellationToken.None,
        TaskCreationOptions.LongRunning | TaskCreationOptions.DenyChildAttach,
        TaskScheduler.Default);
    }

    public void Enqueue(Func<Task> task)
    {
      if (_disposed)
      {
        throw new ObjectDisposedException(nameof(ProcessingThread));
      }

      if (_processingTask == null)
      {
        throw new InvalidOperationException("The processing thread is not started.");
      }

      var taskId = Interlocked.Increment(ref _nextTaskId);
      _tasks.Enqueue(new ProcessingTask(taskId, task));
      _logger.Log(LogLevel.Verbose, $"Task #{taskId} was enqueued.", false);
    }

    public void Dispose()
    {
      _closed = true;
      _disposed = true;
    }

    private async Task ProcessAsync()
    {
      while (!_closed)
      {
        try
        {
          if (_tasks.TryDequeue(out var task))
          {
            _logger.Log(LogLevel.Verbose, $"Started processing task #{task.Id}.", false);
            try
            {
              await task.Task();
              _logger.Log(LogLevel.Verbose, $"Finished processing task #{task.Id}.", false);
            }
            catch (Exception ex)
            {
              _logger.Log(LogLevel.Verbose, $"Exception occured during processing task #{task.Id}. Exception: {ex}", false);
            }
          }
          else
          {
            await Task.Delay(_pollingDelay);
          }
        }
        catch (Exception ex)
        {
          _logger.Log(LogLevel.Verbose, $"Exception occured in processing thread. Exception: {ex}", false);
        }
      }
    }

    private struct ProcessingTask
    {
      public readonly long Id;
      public readonly Func<Task> Task;

      public ProcessingTask(long id, Func<Task> task)
      {
        Id = id;
        Task = task;
      }
    }
  }
}
