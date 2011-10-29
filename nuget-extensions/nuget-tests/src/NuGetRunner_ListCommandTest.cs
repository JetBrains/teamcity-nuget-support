using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListCommandTest
  {
    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_TeamListPublic_Remote(NuGetVersion version)
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.List", "-Id", "NUnit", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_TeamListPublicVersion_Remote(NuGetVersion version)
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.List", "-Id", "NUnit", "-Version", "(1.1.1, 2.5.8)", "-Source", NuGetConstants.DefaultFeedUrl)
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']");

      Assert.IsFalse(r.Output.Contains("Version='2.5.10"));
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_TeamListPublic_Local(NuGetVersion version)
    {
      ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.List", "-Id", "Web", "-Source", Files.GetLocalFeed(version))
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.1.1']",
                              "##teamcity[nuget-package Id='Web' Version='1.2.1']",
                              "##teamcity[nuget-package Id='Web' Version='2.2.2']");
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_TeamListPublicVersion_Local(NuGetVersion version)
    {
      var r = ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version), "TeamCity.List", "-Id", "Web", "-Version", "(1.2.0, 2.1.8)", "-Source", Files.GetLocalFeed(version))
        .Dump()
        .AssertExitedSuccessfully()
        .AssertNoErrorOutput()
        .AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.2.1']");

      Assert.IsFalse(r.Output.Contains("Version='1.1.1"));
      Assert.IsFalse(r.Output.Contains("Version='2.2.2"));
    }

  }

}