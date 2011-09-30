using System;
using System.IO;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunnerLatestPackageTest : NuGetBlackBoxIntegrationTestBase
  {
    private T DownloadAndRun<T>(Func<string, T> withNuGet)
    {
      return TempFilesHolder.WithTempDirectory(
        temp =>
          {
            ProcessExecutor.ExecuteProcess(Files.NuGetExe_1_5, "install", "NuGet.commandline", "-ExcludeVersion", "-OutputDirectory", temp).Dump().AssertNoErrorOutput().AssertExitedSuccessfully();
            string nugetPath = Path.Combine(temp, "NuGet.CommandLine/tools/NuGet.Exe");
            Assert.IsTrue(File.Exists(nugetPath));
            return withNuGet(nugetPath);
          });
    }

    protected override ProcessExecutor.Result DoTest(params string[] argz)
    {
      return DownloadAndRun(path => base.DoTest(path, argz));
    }
  }
}