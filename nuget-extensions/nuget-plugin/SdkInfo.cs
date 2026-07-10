using System;
using System.IO;
using System.Threading.Tasks;
using NuGet.Versioning;

namespace JetBrains.TeamCity.NuGet
{
  internal class SdkInfo
  {
    public async Task<SdkVersion?> GetSdkVersion()
    {
      try
      {
        var sdkPath = Environment.GetEnvironmentVariable("MSBuildSDKsPath");
        if (sdkPath != null)
        {
          var versionFile = Path.Combine(sdkPath, "..", ".version");
          if (File.Exists(versionFile))
          {
            var lines = await ReadVersionFileAsync(versionFile);
            if (lines.Length > 1)
            {
              return new SdkVersion(SemanticVersion.Parse(lines[1]));
            }
          }
        }
      }
      catch
      {
        // ignored
      }
      return null;
    }

    private static Task<string[]> ReadVersionFileAsync(string versionFile)
    {
#if NET5_0_OR_GREATER
      return File.ReadAllLinesAsync(versionFile);
#else
      return Task.FromResult(File.ReadAllLines(versionFile));
#endif
    }
  }

  internal struct SdkVersion
  {
    public SemanticVersion Version;

    public SdkVersion(SemanticVersion version)
    {
      Version = version;
    }
  }
}
