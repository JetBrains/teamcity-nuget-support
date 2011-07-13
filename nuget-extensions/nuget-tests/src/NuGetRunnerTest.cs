using System;
using JetBrains.TeamCity.NuGetRunner;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunnerTest
  {
    private readonly Lazy<string> myRunnerPath = new Lazy<string>(()=>typeof (Program).Assembly.GetAssemblyPath());

    [Test]
    public void TestExcuteNuGet()
    {
      var r = ProcessExecutor.ExecuteProcess(myRunnerPath.Value, NuGet.NuGetPath, "help");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test]
    public void TestDumpExtensionsPath()
    {
      var r = ProcessExecutor.ExecuteProcess(myRunnerPath.Value, NuGet.NuGetPath, "---TeamCity.DumpExtensionsPath");
      Console.Out.WriteLine(r);

      Assert.IsTrue(r.ExitCode == 0);
    }

    [Test]
    public void TestCommand_TeamCityPing()
    {
      ProcessExecutor.ExecuteProcess(myRunnerPath.Value, NuGet.NuGetPath, "TeamCity.Ping")
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("TeamCity NuGet Extension is available.");
    }
  }
}
