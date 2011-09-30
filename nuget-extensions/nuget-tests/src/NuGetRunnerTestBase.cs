using System;
using System.IO;
using System.Linq;
using System.Threading;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public abstract class NuGetRunnerTestBase : NuGetIntegrationTestBase
  {
    [Test]
    public void TestExcuteNuGet()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "help");
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
            File.Copy(NuGetExe, destNuGet);

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
            
            File.Copy(NuGetExe, destNuGet);            
            File.Copy(Files.NuGetRunnerExe, destRunner);
            const string ext = "JetBrains.TeamCity.NuGet.ExtendedCommands.dll";
            Directory.CreateDirectory(Path.Combine(home, "plugins4"));
            File.Copy(Path.Combine(Files.NuGetRunnerExe, "../plugins4/" + ext), Path.Combine(home, "plugins4/" + ext));

            ProcessExecutor.ExecuteProcess(destRunner, destNuGet, "TeamCity.Ping")
              .Dump()
              .AssertExitedSuccessfully();
          });
    }


    [Test]
    public void TestDumpExtensionsPath()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "---TeamCity.DumpExtensionsPath");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test]
    public void TestCommand_TeamCityPing()
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "TeamCity.Ping")
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("TeamCity NuGet Extension is available.");
    }

    [Test]
    public void TestCommands_RunConcurrently()
    {
      bool failed = false;
      new[] {1, 2, 3, 4}
        .Select(
          x => new Thread(() =>
                            {
                              for (int i = 0; i < 3; i++)
                              {
                                var proc = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "TeamCity.Ping", "-Sleep");
                                if (proc.ExitCode != 0 || !string.IsNullOrWhiteSpace(proc.Error))
                                {
                                  proc.Dump();
                                  failed = true;
                                }
                                proc.AssertNoErrorOutput().AssertExitedSuccessfully();
                              }
                            })
                 {
                   Name = "Checker " + x,
                 })
        .ToList()
        .Select(x => { x.Start(); return x; })
        .ToList()
        .Select(x => { x.Join(); return 0; })
        .ToList();

      Assert.IsFalse(failed);
    }

  }
}