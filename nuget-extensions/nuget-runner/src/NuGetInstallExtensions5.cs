using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetInstallExtensions5 : NuGetInstallExtensionsBase
  {
    public NuGetInstallExtensions5(NuGetRunner runner, IEnumerable<string> extensions) : base(runner)
    {
      string extensionPaths = string.Join("; ", extensions.Select(Path.GetDirectoryName).Select(Path.GetFullPath).Where(NotNull).Distinct());
      Console.Out.WriteLine("Registered additional extensions from paths: {0}", extensionPaths);
      runner.AddEnvironmentVariable("NUGET_EXTENSIONS_PATH", extensionPaths);

      runner.BeforeNuGetStarted += (_,__) => CleanupHome();
    }

    private static bool NotNull(object x)
    {
      return x != null;
    }
  }
}