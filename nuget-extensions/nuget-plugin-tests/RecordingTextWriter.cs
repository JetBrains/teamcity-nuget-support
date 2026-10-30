using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class RecordingTextWriter : TextWriter
  {
    private readonly object _lock = new object();
    private readonly List<string> _lines = new List<string>();

    public override Encoding Encoding => Encoding.UTF8;

    public IList<string> Lines
    {
      get
      {
        lock (_lock)
        {
          return _lines.ToList();
        }
      }
    }

    public int FlushCount { get; private set; }

    public override void WriteLine(string value)
    {
      lock (_lock)
      {
        _lines.Add(value);
      }
    }

    public override void Flush()
    {
      FlushCount++;
    }

    public void Clear()
    {
      lock (_lock)
      {
        _lines.Clear();
        FlushCount = 0;
      }
    }

    public bool WaitForLineCount(int count, TimeSpan timeout)
    {
      var deadline = DateTime.UtcNow + timeout;
      while (DateTime.UtcNow < deadline)
      {
        lock (_lock)
        {
          if (_lines.Count >= count)
          {
            return true;
          }
        }

        Thread.Sleep(10);
      }

      return false;
    }
  }
}
