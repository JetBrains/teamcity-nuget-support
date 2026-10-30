using System;

namespace JetBrains.TeamCity.NuGet.Plugin.Tests
{
  internal sealed class DelegateDisposable : IDisposable
  {
    private readonly Action _dispose;

    public DelegateDisposable(Action dispose)
    {
      _dispose = dispose;
    }

    public void Dispose()
    {
      _dispose();
    }
  }
}
