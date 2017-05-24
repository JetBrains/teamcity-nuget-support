using System;
using System.IO;
using System.Threading;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetInstallExtensionsBase
  {
    private readonly Lazy<string> NuGetExtensionsHome;

    protected NuGetInstallExtensionsBase(NuGetRunner runner)
    {
      NuGetExtensionsHome = new Lazy<string>(() => Path.Combine(runner.LocateNuGetExtensionsPath(), "TeamCity.Extensions"));
    }

    protected string NuGetSharedExntensions
    {
      get { return NuGetExtensionsHome.Value; }
    }

    protected bool CleanupHome()
    {
      string home = NuGetSharedExntensions;
      for (int i = 0; i < 10; i++)
      {
        try
        {
          if (Directory.Exists(home))
            Directory.Delete(home, true);
          else
            return true;
        }
        catch
        {
          var span = TimeSpan.FromSeconds(5);
          Console.Out.WriteLine("Failed to delete " + home + ". Will retry in " + span.TotalSeconds + " seconds");
          Thread.Sleep(span);
        }
      }
      return false;
    }
  }
}