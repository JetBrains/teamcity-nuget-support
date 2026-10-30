using System;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.PluginHost
{
  internal interface IPluginSession : IDisposable
  {
    event EventHandler<PluginFaultedEventArgs> Faulted;
    event EventHandler BeforeClose;
    event EventHandler Closed;

    ILogger CreateLogger();
  }

  internal sealed class PluginFaultedEventArgs : EventArgs
  {
    public PluginFaultedEventArgs(string message, Exception exception)
    {
      Message = message;
      Exception = exception;
    }

    public string Message { get; }
    public Exception Exception { get; }
  }
}
