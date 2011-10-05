using System.Collections.Generic;
using System.IO;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetInstallExtensions4 : NuGetInstallExtensionsBase
  {
    public NuGetInstallExtensions4(NuGetRunner runner, IEnumerable<string> extensions) : base(runner)
    {      
      runner.BeforeNuGetStarted += (_, __) =>
                                     {
                                       string home = NuGetSharedExntensions;
                                       CleanupHome();

                                       if (!Directory.Exists(home))
                                         Directory.CreateDirectory(home);

                                       foreach (var ext in extensions)
                                       {
                                         var destFileName = Path.Combine(home, Path.GetFileName(ext));
                                         File.Copy(ext, destFileName);
                                       }
                                     };

      runner.AfterNuGetFinished += (_, __) => CleanupHome();
    }
  }
}