using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{  
  [TestFixture("bt4")]
  public class NuGetRunnerWithTrunk
  {
    private readonly Lazy<string> home;
    
    private readonly Lazy<string> nuget;

    public NuGetRunnerWithTrunk(string bt)
    {

      home = new Lazy<string>(() =>
                                {
                                  var homePath = Path.GetTempFileName();
                                  File.Delete(homePath);
                                  Directory.CreateDirectory(homePath);
                                  return homePath;
                                });
      
      nuget = new Lazy<string>(() =>
                                 {
                                   string url = "http://ci.nuget.org:8080/guestAuth/repository/download/" + bt +
                                                "/.lastSuccessful/Console/NuGet.exe";
                                   var nugetPath = Path.Combine(home.Value, "NuGet.exe");
                                   var cli = new WebClient();
                                   cli.DownloadFile(url, nugetPath);
                                   return nugetPath;
                                 });
      
     
    }

    [TestFixtureTearDown]
    public void Cleanup()
    {
      if (home.IsValueCreated)
      {
        Directory.Delete(home.Value, true);
      }
    }

    [Test]
    public void Test_Ping()
    {
      DoTest("TeamCity.Ping");
    }

    [Test]
    public void Test_List_NUnit_Remote()
    {
      DoTest("TeamCity.List", "-Id", "NUnit")
        .AssertOutputContains("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    }

    [Test]
    public void Test_List_NUnit_Local()
    {
      DoTest("TeamCity.List", "-Id", "Web", "-Source", Files.LocalFeed)
        .AssertOutputContains("##teamcity[nuget-package Id='Web' Version='1.1.1']");
    }
    
    private ProcessExecutor.Result DoTest(params string[] argz)
    {
      var az = new List<string>();
      az.Add(nuget.Value);
      az.AddRange(argz);

      return ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, az.ToArray())
        .Dump()
        .AssertExitedSuccessfully();
    }
  }
}