using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public abstract class NuGetRunner_ListCommandTest_Remote_Base : NuGetIntegrationTestBase 
  {
    [Test]
    public void TestCommand_TeamListPublic()
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "TeamCity.List", "-Id", "NUnit", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    }

    [Test]
    public void TestCommand_TeamListPublicVersion()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "TeamCity.List", "-Id", "NUnit", "-Version", "(1.1.1, 2.5.8)", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']");

      Assert.IsFalse(r.Output.Contains("Version='2.5.10"));
    }

  }
}