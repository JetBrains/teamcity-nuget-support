using System;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  internal sealed class PluginTimeouts
  {
    public const string HandshakeTimeoutEnvironmentVariable = "NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS";
    public const string RequestTimeoutEnvironmentVariable = "NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS";
    public const string ShutdownTimeoutEnvironmentVariable = "NUGET_PLUGIN_SHUTDOWN_TIMEOUT_IN_SECONDS";
    
    private static readonly TimeSpan DefaultHandshakeTimeout = TimeSpan.FromSeconds(30);
    private static readonly TimeSpan DefaultRequestTimeout = TimeSpan.FromSeconds(30);
    private static readonly TimeSpan DefaultShutdownTimeout = TimeSpan.FromSeconds(120);
    private static readonly TimeSpan MinTimeout = TimeSpan.FromTicks(1);
    private static readonly TimeSpan MaxTimeout = TimeSpan.FromMilliseconds(int.MaxValue);
    private static readonly PluginTimeouts _instance = new PluginTimeouts();

    public static PluginTimeouts Instance => _instance;

    public TimeSpan HandshakeTimeout { get; private set; }
    public TimeSpan RequestTimeout { get; private set; }
    public TimeSpan ShutdownTimeout { get; private set; }

    private PluginTimeouts()
    {
      HandshakeTimeout = GetTimeout(Environment.GetEnvironmentVariable(HandshakeTimeoutEnvironmentVariable),
        DefaultHandshakeTimeout);
      RequestTimeout = GetTimeout(Environment.GetEnvironmentVariable(RequestTimeoutEnvironmentVariable),
        DefaultRequestTimeout);
      ShutdownTimeout = GetTimeout(Environment.GetEnvironmentVariable(ShutdownTimeoutEnvironmentVariable),
        DefaultShutdownTimeout);
    }

    internal static void SetTimeouts(TimeSpan handshakeTimeout, TimeSpan requestTimeout)
    {
      if (!IsValidTimeout(handshakeTimeout))
      {
        throw new ArgumentOutOfRangeException(nameof(handshakeTimeout));
      }

      if (!IsValidTimeout(requestTimeout))
      {
        throw new ArgumentOutOfRangeException(nameof(requestTimeout));
      }

      Instance.HandshakeTimeout = handshakeTimeout;
      Instance.RequestTimeout = requestTimeout;
    }

    private static TimeSpan GetTimeout(string timeoutInSeconds, TimeSpan fallbackTimeout)
    {
      if (!int.TryParse(timeoutInSeconds, out var seconds)) return fallbackTimeout;
      try
      {
        var timeout = TimeSpan.FromSeconds(seconds);
        if (IsValidTimeout(timeout)) return timeout;
      }
      catch
      {
        // ignored
      }

      return fallbackTimeout;
    }

    private static bool IsValidTimeout(TimeSpan timeout)
    {
      return MinTimeout <= timeout && timeout <= MaxTimeout;
    }
  }
}
