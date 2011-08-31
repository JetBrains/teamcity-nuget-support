using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public abstract class NuGetRunner_ListCommandTest_Local_Base : NuGetIntegrationTestBase
  {
    [Test]
    public void TestCommand_TeamListPublic()
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "TeamCity.List", "-Id", "Web", "-Source", Files.LocalFeed)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.1.1']",
                              "##teamcity[nuget-package Id='Web' Version='1.2.1']",
                              "##teamcity[nuget-package Id='Web' Version='2.2.2']");
    }

    [Test]
    public void TestCommand_TeamListPublicVersion()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, NuGetExe, "TeamCity.List", "-Id", "Web", "-Version", "(1.2.0, 2.1.8)", "-Source", Files.LocalFeed)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.2.1']");

      Assert.IsFalse(r.Output.Contains("Version='1.1.1"));
      Assert.IsFalse(r.Output.Contains("Version='2.2.2"));
    }
  }
}