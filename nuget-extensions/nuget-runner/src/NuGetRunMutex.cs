using System.Threading;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetRunMutex
  {
    public NuGetRunMutex(NuGetRunner runner)
    {
      var m = new Mutex(false, "JetBrains.TeamCity.NuGet.RunMutex");
      runner.BeforeNuGetStarted += (_, __) => m.WaitOne();
      runner.BeforeNuGetStarted += (_, __) => m.ReleaseMutex();
    }
  }
}