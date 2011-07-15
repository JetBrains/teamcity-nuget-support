using System.IO;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [Ignore("Not yet needed in TeamCity NuGet support")]
  [TestFixture]
  public class NuGetRunner_ListPackagesCommandTest_Remote
  {
    [Test]
    public void TestCommand_ListPublic()
    {
      TempFilesHolder.WithTempFile(
        file =>
          {
            File.WriteAllText(file,
                              "<NuGet-Request Source=\"" + NuGetConstants.DefaultFeedUrl +
                              "\"><Requests><Request Id='NUnit'/></Requests></NuGet-Request>");

            ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe,
                                           "TeamCity.ListPackages", "-Request", file)
              .Dump()
              .AssertExitedSuccessfully()
              .AssertNoErrorOutput()
              .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']")
              .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']")
              ;
          });
    }

    [Test]
    public void TestCommand_ListPublicVersions()
    {
      TempFilesHolder.WithTempFile(
        file =>
          {
            File.WriteAllText(file,
                              "<NuGet-Request Source=\"" + NuGetConstants.DefaultFeedUrl +
                              "\"><Requests><Request Id='NUnit' Versions='(1.1.1,2.5.8]'/></Requests></NuGet-Request>");

            ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.NuGetExe,
                                           "TeamCity.ListPackages", "-Request", file)
              .Dump()
              .AssertExitedSuccessfully()
              .AssertNoErrorOutput()
              .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']")
              ;
          });
    }

  }
}