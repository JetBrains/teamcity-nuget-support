﻿using System;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunnerTest
  {
    private static Regex myExtensionsPattern = new Regex(
      @"TeamCity.NuGetVersion: \d\.\d", RegexOptions.Compiled | RegexOptions.Multiline);

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestExcuteNuGet(NuGetVersion version)
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "help");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
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

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestDumpExtensionsPath(NuGetVersion version)
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "---TeamCity.DumpExtensionsPath");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_TeamCityPing(NuGetVersion version)
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.Ping")
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("TeamCity NuGet Extension is available.");
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_TeamCityNOOP_Sources(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
        sources =>
          {
            File.WriteAllText(sources,
                              @"<sources> <source source=""http://localhost:1025/nuget/"" username=""u-WXjhnQSiZ1Ks3j3vqF2w11lCzeXJgqfS"" password=""p-9tZNW3k2DQhiPm76V5iVi2F3R25DO1PJ"" /></sources>");
            Environment.SetEnvironmentVariable("TEAMCITY_NUGET_FEEDS", sources);
            try
            {

              var exec = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                                        "TeamCity.NOOP")
                                        .Dump()
                                        .AssertExitedSuccessfully()
                                        .AssertNoErrorOutput()
                                        .AssertOutputContains("NuGetTeamCityInfo")
                                        ;

              if (version < NuGetVersion.NuGet_2_0)
              {
                exec.AssertOutputContains("Feed authentication is only supported", "##teamcity");
              }
              else if (version < NuGetVersion.NuGet_3_3)
              {
                exec.AssertOutputContains("ENABLED:feed=http://localhost:1025/nuget/,user=u-WXjhnQSiZ1Ks3j3vqF2w11lCzeXJgqfS");
              }
            }
            finally
            {
              Environment.SetEnvironmentVariable("TEAMCITY_NUGET_FEEDS", "");
            }
          });
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_NuGetVersion(NuGetVersion version)
    {

      var res =
        ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "--TeamCity.NuGetVersion")
          .Dump()
          .AssertExitedSuccessfully()
          .AssertNoErrorOutput();

      Assert.IsTrue(myExtensionsPattern.IsMatch(res.Output));
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
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

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestExcuteNuGet_ShouldVerifyNuGetAssembly(NuGetVersion version)
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var destNuGet = Path.Combine(home, "NuGet.exe");
          File.Copy(Files.GetNuGetExe(version), destNuGet);

          ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, "-v", destNuGet, "TeamCity.Ping")
            .Dump()
            .AssertExitedSuccessfully();
        });
    }

    [Test]
    public void TestExcuteNuGet_ShouldVerifyWrongNuGetAssemblyName()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var destNuGet = Path.Combine(home, "NuGet.exe");
          File.Copy(Files.WrongAssemblyNameNuGet, destNuGet);

          ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, "-v", destNuGet, "TeamCity.Ping")
            .Dump()
            .AssertErrorContains("Unexpected NuGet assembly: CsLex, Version=777.0.0.0, Culture=neutral, PublicKeyToken=1010a0d8d6380325")
            .AssertExitCode(2);
        });
    }

    [Test]
    public void TestExcuteNuGet_ShouldVerifyWrongNuGetAssemblyStrongName()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var destNuGet = Path.Combine(home, "NuGet.exe");
          File.Copy(Files.WrongAssemblyStrongNameNuGet, destNuGet);

          ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, "-v", destNuGet, "TeamCity.Ping")
            .Dump()
            .AssertErrorContains("Unexpected NuGet assembly: CsLex, Version=777.0.0.0, Culture=neutral, PublicKeyToken=1010a0d8d6380325")
            .AssertExitCode(2);
        });
    }
  }
}
