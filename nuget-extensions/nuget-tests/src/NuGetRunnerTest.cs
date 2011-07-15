using System;
using System.IO;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunnerTest
  {
    [Test]
    public void TestExcuteNuGet()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "help");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test]
    public void TestExcuteNuGet_NuGetFromTemp()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
          {
            var destNuGet = Path.Combine(home, "NuGet.exe");
            File.Copy(Files.NuGetExe, destNuGet);

            ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, destNuGet, "TeamCity.Ping")
              .Dump()
              .AssertExitedSuccessfully();
          });
    }

    [Test]
    public void TestExcuteNuGet_BothInTemp()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
          {
            var destNuGet = Path.Combine(home, "NuGet.exe");
            var destRunner = Path.Combine(home, "TeamCity.NuGetRunner.exe");
            
            File.Copy(Files.NuGetExe, destNuGet);            
            File.Copy(Files.NuGetRunnerExe, destRunner);
            const string ext = "JetBrains.TeamCity.NuGet.ExtendedCommands.dll";
            File.Copy(Path.Combine(Files.NuGetRunnerExe, "../" + ext), Path.Combine(home, ext));

            ProcessExecutor.ExecuteProcess(destRunner, destNuGet, "TeamCity.Ping")
              .Dump()
              .AssertExitedSuccessfully();
          });
    }


    [Test]
    public void TestDumpExtensionsPath()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "---TeamCity.DumpExtensionsPath");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test]
    public void TestCommand_TeamCityPing()
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "TeamCity.Ping")
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("TeamCity NuGet Extension is available.");
    }
  }
}
