using System.IO;
using NUnit.Framework;
using NuGet;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListPackagesCommandTest
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

            ProcessExecutor.ExecuteProcess(NuGetRunner.Path.Value, NuGet.NuGetPath,
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

            ProcessExecutor.ExecuteProcess(NuGetRunner.Path.Value, NuGet.NuGetPath,
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