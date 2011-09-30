using System;
using System.Collections.Generic;
using System.IO;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetInstallExtensions4
  {
    public NuGetInstallExtensions4(NuGetRunner runner, IEnumerable<string> extensions)
    {
      Func<string> computeHome = () => Path.Combine(runner.LocateNuGetExtensionsPath(), "TeamCity.Extensions");
      runner.BeforeNuGetStarted += (_, __) =>
                                     {
                                       string home = computeHome();
                                       if (Directory.Exists(home))
                                         Directory.Delete(home, true);

                                       if (!Directory.Exists(home))
                                         Directory.CreateDirectory(home);

                                       foreach (var ext in extensions)
                                       {
                                         var destFileName = Path.Combine(home, Path.GetFileName(ext));
                                         File.Copy(ext, destFileName);
                                       }
                                     };

      runner.AfterNuGetFinished += (_, __) =>
                                     {
                                       string home = computeHome();
                                       if (Directory.Exists(home))
                                         Directory.Delete(home, true);
                                     };
    }
  }
}