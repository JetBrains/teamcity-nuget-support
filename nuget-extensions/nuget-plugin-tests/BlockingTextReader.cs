using System.Collections.Concurrent;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class BlockingTextReader : TextReader
  {
    private readonly BlockingCollection<ReadLineResult> _lines = new BlockingCollection<ReadLineResult>();

    public void AddLine(string line)
    {
      _lines.Add(new ReadLineResult(line));
    }

    public void Complete()
    {
      if (!_lines.IsAddingCompleted)
      {
        AddLine(null);
        _lines.CompleteAdding();
      }
    }

    public override Task<string> ReadLineAsync()
    {
      return Task.Factory.StartNew(
        () => _lines.Take().Line,
        CancellationToken.None,
        TaskCreationOptions.None,
        TaskScheduler.Default);
    }

    protected override void Dispose(bool disposing)
    {
      if (disposing)
      {
        Complete();
        _lines.Dispose();
      }

      base.Dispose(disposing);
    }

    private sealed class ReadLineResult
    {
      public ReadLineResult(string line)
      {
        Line = line;
      }

      public string Line { get; private set; }
    }
  }
}
