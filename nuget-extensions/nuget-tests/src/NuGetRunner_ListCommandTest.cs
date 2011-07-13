using NUnit.Framework;
using NuGet;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListCommandTest
  {
    [Test]
    public void TestCommand_TeamListPublic()
    {
      ProcessExecutor.ExecuteProcess(NuGetRunner.Path.Value, NuGet.NuGetPath, "TeamCity.List", "-Id", "NUnit", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    }

    [Test]
    public void TestCommand_TeamListPublicVersion()
    {
      var r = ProcessExecutor.ExecuteProcess(NuGetRunner.Path.Value, NuGet.NuGetPath, "TeamCity.List", "-Id", "NUnit", "-Version", "(1.1.1, 2.5.8)", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']");

      Assert.IsFalse(r.Output.Contains("Version='2.5.10"));
    }
  }
}