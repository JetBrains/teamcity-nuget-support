using System;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Connectivity;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.PluginHost
{
  internal class PluginSessionFactory
  {
    private readonly ILogger _logger;

    public PluginSessionFactory(ILogger logger)
    {
      _logger = logger;
    }
    
    public async Task<IPluginSession> CreateAsync(
      IRequestHandlers requestHandlers,
      CancellationToken cancellationToken)
    {
      var plugin = await PluginProtocolSession
        .CreateFromCurrentProcessAsync(requestHandlers, cancellationToken, _logger)
        .ConfigureAwait(false);

      return new PluginSession(plugin);
    }

    private sealed class PluginSession : IPluginSession
    {
      private readonly PluginProtocolSession _plugin;

      public PluginSession(PluginProtocolSession plugin)
      {
        _plugin = plugin;
        _plugin.Faulted += OnFaulted;
        _plugin.BeforeClose += OnBeforeClose;
        _plugin.Closed += OnClosed;
      }

      public event EventHandler<PluginFaultedEventArgs> Faulted;
      public event EventHandler BeforeClose;
      public event EventHandler Closed;

      public ILogger CreateLogger()
      {
        return new PluginConnectionLogger(_plugin.Connection);
      }

      public void Dispose()
      {
        _plugin.Faulted -= OnFaulted;
        _plugin.BeforeClose -= OnBeforeClose;
        _plugin.Closed -= OnClosed;
        _plugin.Dispose();
      }

      private void OnFaulted(object sender, ProtocolErrorEventArgs args)
      {
        var message = args.Message == null
          ? null
          : $"{args.Message.Type} {args.Message.Method} {args.Message.RequestId}";

        Faulted?.Invoke(this, new PluginFaultedEventArgs(message, args.Exception));
      }

      private void OnBeforeClose(object sender, EventArgs args)
      {
        BeforeClose?.Invoke(this, EventArgs.Empty);
      }

      private void OnClosed(object sender, EventArgs args)
      {
        Closed?.Invoke(this, EventArgs.Empty);
      }
    }
  }
}
