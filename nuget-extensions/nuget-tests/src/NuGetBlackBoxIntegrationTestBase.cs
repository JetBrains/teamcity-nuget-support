using System.Collections.Generic;
using System.IO;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public abstract class NuGetBlackBoxIntegrationTestBase
  {
    [Test]
    public void Test_Ping()
    {
      DoTest("TeamCity.Ping");
    }

    [Test]
    public void Test_List_NUnit_Remote()
    {
      DoTest("TeamCity.List", "-Id", "NUnit").AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    }

    [Test]
    public void Test_List_NUnit_Local()
    {
      DoTest("TeamCity.List", "-Id", "Web", "-Source", Files.LocalFeed).AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.1.1']");
    }

    [Test]
    public void TestCommand_ListPublic()
    {
      TempFilesHolder.WithTempFile(
        file =>
        {
          File.WriteAllText(file,
                            "<NuGet-Request Source=\"" + NuGetConstants.DefaultFeedUrl +
                            "\"><Requests><Request Id='NUnit'/></Requests></NuGet-Request>");

          DoTest("TeamCity.ListPackages", "-Request", file)
            .AssertNoErrorOutput()
            .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.7.10213']")
            .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']")
            ;
        });
    }


    protected abstract ProcessExecutor.Result DoTest(params string[] argz);

    protected virtual ProcessExecutor.Result DoTest(string nugetPath, IEnumerable<string> argz)
    {
      var az = new List<string> {nugetPath};
      az.AddRange(argz);

      return ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, az.ToArray())
        .Dump()
        .AssertExitedSuccessfully();
    }
  }
}