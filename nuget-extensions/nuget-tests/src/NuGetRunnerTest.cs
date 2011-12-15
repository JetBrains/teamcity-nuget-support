using System;
using System.IO;
using System.Linq;
using System.Threading;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunnerTest
  {
    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_16_CI)]
    public void TestExcuteNuGet(NuGetVersion version)
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "help");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_16_CI)]
    public void TestExcuteNuGet_NuGetFromTemp(NuGetVersion version)
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var destNuGet = Path.Combine(home, "NuGet.exe");
          File.Copy(Files.GetNuGetExe(version), destNuGet);

          ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, destNuGet, "TeamCity.Ping")
            .Dump()
            .AssertExitedSuccessfully();
        });
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_16_CI)]
    public void TestExcuteNuGet_BothInTemp(NuGetVersion version)
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var destNuGet = Path.Combine(home, "NuGet.exe");
          var destRunner = Path.Combine(home, "TeamCity.NuGetRunner.exe");

          File.Copy(Files.GetNuGetExe(version), destNuGet);
          File.Copy(Files.NuGetRunnerExe, destRunner);
          const string ext = "JetBrains.TeamCity.NuGet.ExtendedCommands.dll";
          Directory.CreateDirectory(Path.Combine(home, "plugins4"));
          File.Copy(Path.Combine(Files.NuGetRunnerExe, "../plugins4/" + ext), Path.Combine(home, "plugins4/" + ext));

          ProcessExecutor.ExecuteProcess(destRunner, destNuGet, "TeamCity.Ping")
            .Dump()
            .AssertExitedSuccessfully();
        });
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_16_CI)]
    public void TestDumpExtensionsPath(NuGetVersion version)
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "---TeamCity.DumpExtensionsPath");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_16_CI)]
    public void TestCommand_TeamCityPing(NuGetVersion version)
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.Ping")
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("TeamCity NuGet Extension is available.");
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_16_CI)]
    public void TestCommands_RunConcurrently(NuGetVersion version)
    {
      bool failed = false;
      new[] { 1, 2, 3, 4 }
        .Select(
          x => new Thread(() =>
          {
            for (int i = 0; i < 3; i++)
            {
              var proc = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.Ping", "-Sleep");
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
