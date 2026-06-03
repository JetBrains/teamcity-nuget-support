using System;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Connectivity;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class CancelAwareRequestHandler : IRequestHandler
  {
    public CancelAwareRequestHandler()
    {
      Started = new ManualResetEvent(false);
      Canceled = new ManualResetEvent(false);
    }

    public ManualResetEvent Started { get; }
    public ManualResetEvent Canceled { get; }

    public CancellationToken CancellationToken => CancellationToken.None;

    public Task HandleResponseAsync(
      IConnection connection,
      Message message,
      IResponseHandler responseHandler,
      CancellationToken cancellationToken)
    {
      Started.Set();
      return Task.Factory.StartNew(
        () =>
        {
          cancellationToken.WaitHandle.WaitOne();
          Canceled.Set();
          throw new OperationCanceledException(cancellationToken);
        },
        CancellationToken.None,
        TaskCreationOptions.None,
        TaskScheduler.Default);
    }
  }
}
