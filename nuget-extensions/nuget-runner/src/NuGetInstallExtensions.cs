using System;
using System.Collections.Generic;
using System.IO;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetInstallExtensions
  {
    public NuGetInstallExtensions(NuGetRunner runner, IEnumerable<string> extensions)
    {
      Func<string> computeHome = () => Path.Combine(runner.NuGetExtensionsPath.Value, "TeamCity.Extensions");
      runner.BeforeNuGetStarted += (_, __) =>
                                     {
                                       string home = computeHome();
                                       if (!Directory.Exists(home))
                                         Directory.CreateDirectory(home);

                                       foreach (var ext in extensions)
                                       {
                                         File.Copy(ext, Path.Combine(home, Path.GetFileName(ext)));
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