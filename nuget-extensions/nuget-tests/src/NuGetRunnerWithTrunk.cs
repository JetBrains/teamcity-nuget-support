using System;
using System.IO;
using System.Net;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{ 
  [TestFixture]
  public class NuGetRunnerWithTrunk : NuGetBlackBoxIntegrationTestBase
  {
    private readonly Lazy<string> home;    
    private readonly Lazy<string> nuget;

    public NuGetRunnerWithTrunk()
    {
      const string bt = "bt4";

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

    protected override ProcessExecutor.Result DoTest(params string[] argz)
    {
      return DoTest(nuget.Value, argz);
    }
  }
}