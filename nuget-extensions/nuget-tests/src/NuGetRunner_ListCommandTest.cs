using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListCommandTest_Remote
  {
    [Test]
    public void TestCommand_TeamListPublic()
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "TeamCity.List", "-Id", "NUnit", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    }

    [Test]
    public void TestCommand_TeamListPublicVersion()
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "TeamCity.List", "-Id", "NUnit", "-Version", "(1.1.1, 2.5.8)", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']");

      Assert.IsFalse(r.Output.Contains("Version='2.5.10"));
    }
  }

  [TestFixture]
  public class NuGetRunner_ListCommandTest_Local
  {
    [Test]
    public void TestCommand_TeamListPublic()
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "TeamCity.List", "-Id", "Web", "-Source", Files.LocalFeed)
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
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe, "TeamCity.List", "-Id", "Web", "-Version", "(1.2.0, 2.1.8)", "-Source", Files.LocalFeed)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.2.1']");

      Assert.IsFalse(r.Output.Contains("Version='1.1.1"));
      Assert.IsFalse(r.Output.Contains("Version='2.2.2"));
    }
  }
}