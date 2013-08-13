using System;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class DisposableDelegate : IDisposable
  {
    private readonly Action myDispose;

    public DisposableDelegate(Action dispose)
    {
      myDispose = dispose;
    }

    public void Dispose()
    {
      try
      {
        myDispose();
      }
      catch (Exception e)
      {
        Console.Out.WriteLine("Failed to dispose. " + e.Message);
        Console.Out.WriteLine(e);
      }
    }
  }
}
