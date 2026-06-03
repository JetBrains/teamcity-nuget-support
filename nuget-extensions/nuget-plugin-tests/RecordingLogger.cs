using System.Collections.Generic;
using System.Linq;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Logging;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class RecordingLogger : ILogger
  {
    private readonly object _lock = new object();
    private readonly List<string> _messages = new List<string>();

    public IList<string> Messages
    {
      get
      {
        lock (_lock)
        {
          return _messages.ToList();
        }
      }
    }

    public void Log(LogLevel level, string message,bool notifyNuGet = true)
    {
      lock (_lock)
      {
        _messages.Add(message);
      }
    }

    public void SetLogLevel(LogLevel newLogLevel)
    {
    }

    public void Clear()
    {
      lock (_lock)
      {
        _messages.Clear();
      }
    }
  }
}
