using System;
using System.IO;
using NuGet.Versioning;

namespace JetBrains.TeamCity.NuGet
{
  internal class SdkInfo
  {
    public bool TryGetSdkVersion(out SemanticVersion version)
    {
      try
      {
        var sdkPath = Environment.GetEnvironmentVariable("MSBuildSDKsPath");
        if (sdkPath != null)
        {
          var versionFile = Path.Combine(sdkPath, "..", ".version");
          if (File.Exists(versionFile))
          {
            var lines = File.ReadAllLines(versionFile);
            if (lines.Length > 1)
            {
              version = SemanticVersion.Parse(lines[1]);
              return true;
            }
          }
        }
      }
      catch
      {
        // ignored
      }

      version = default(SemanticVersion);
      return false;
    }
  }
}
