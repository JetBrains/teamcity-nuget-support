// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class InboundRequestProcessingHandler : IDisposable
  {
    private readonly ISet<MessageMethod> _fastProcessingMethods;
    private readonly ILogger _logger;
    private readonly Lazy<ProcessingThread> _processingThread;
    private bool _disposed;

    public InboundRequestProcessingHandler(IEnumerable<MessageMethod> fastProcessingMethods, ILogger logger)
    {
      _fastProcessingMethods = new HashSet<MessageMethod>(fastProcessingMethods ?? Enumerable.Empty<MessageMethod>());
      _logger = logger;
      _processingThread = new Lazy<ProcessingThread>(() =>
                                                              {
                                                                var thread = new ProcessingThread(TimeSpan.FromMilliseconds(50), logger);
                                                                thread.Start();
                                                                return thread;
                                                              });
    }

    public void Handle(MessageMethod method, Func<Task> task, CancellationToken cancellationToken)
    {
      if (_disposed)
      {
        throw new ObjectDisposedException(nameof(InboundRequestProcessingHandler));
      }

      if (_fastProcessingMethods.Contains(method))
      {
        _logger.Log(LogLevel.Verbose, $"Starting request processing task for '{method}' on the processing thread.", false);
        _processingThread.Value.Enqueue(task);
      }
      else
      {
        _logger.Log(LogLevel.Verbose, $"Starting request processing task for '{method}'.", false);
        Task.Run(task, cancellationToken);
      }
    }

    public void Dispose()
    {
      if (_disposed)
      {
        return;
      }

      if (_processingThread.IsValueCreated)
      {
        _processingThread.Value.Dispose();
      }

      _disposed = true;
    }
  }
}
