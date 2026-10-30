using System;
using System.IO;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class TemporaryDirectory : IDisposable
  {
    public TemporaryDirectory()
    {
      Path = System.IO.Path.Combine(System.IO.Path.GetTempPath(), "nuget-plugin-tests-" + Guid.NewGuid().ToString("N"));
      Directory.CreateDirectory(Path);
    }

    public string Path { get; }

    public void Dispose()
    {
      try
      {
        if (Directory.Exists(Path))
        {
          Directory.Delete(Path, true);
        }
      }
      catch
      {
        // ignored
      }
    }
  }
}
