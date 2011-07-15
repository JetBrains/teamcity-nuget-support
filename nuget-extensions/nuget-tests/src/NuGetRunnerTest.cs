using System;
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
