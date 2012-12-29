using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class Files
  {
    private static readonly Lazy<string> ourCachedNuGetExe_1_4 = PathSearcher.SearchFile("lib/nuget/1.4/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_5 = PathSearcher.SearchFile("lib/nuget/1.5/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_6 = PathSearcher.SearchFile("lib/nuget/1.6/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_7 = PathSearcher.SearchFile("lib/nuget/1.7/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_8 = PathSearcher.SearchFile("lib/nuget/1.8/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_0 = PathSearcher.SearchFile("lib/nuget/2.0/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_1 = PathSearcher.SearchFile("lib/nuget/2.1/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_2 = PathSearcher.SearchFile("lib/nuget/2.2/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetRunnerPath = PathSearcher.SearchFile("JetBrains.TeamCity.NuGetRunner.exe", "bin/JetBrains.TeamCity.NuGetRunner.exe");
    private static readonly Lazy<string> ourLocalFeed = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed");
    private static readonly Lazy<string> ourLocalFeed_1_4 = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed_1.4");
    private static readonly Lazy<string> ourLocalFeed_1_8 = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed_1.8");    
    private static readonly Lazy<string> ourCachedNuGet_CI_Last = new Lazy<string>(() => FetchLatestNuGetPackage("bt43"));
    private static readonly Lazy<string> ourCachedNuGet_CI_2_2 = new Lazy<string>(() => FetchLatestNuGetPackage("bt42"));
    private static readonly Lazy<string> ourCachedNuGet_CommandLinePackage_Last = new Lazy<string>(FetchLatestNuGetCommandline); 

    public static string GetLocalFeedURI(NuGetVersion version)
    {
      return new Uri(GetLocalFeed(version)).ToString();
    }

    public static string GetLocalFeed(NuGetVersion version)
    {
      return version.Is_1_4() ? ourLocalFeed_1_4.Value : ourLocalFeed.Value;
    }

    public static string GetLocalFeed_1_8()
    {
      return ourLocalFeed_1_8.Value;
    }
    
    public static string NuGetExe_1_4 { get { return ourCachedNuGetExe_1_4.Value; } }
    public static string NuGetExe_1_5 { get { return ourCachedNuGetExe_1_5.Value; } }
    public static string NuGetExe_1_6 { get { return ourCachedNuGetExe_1_6.Value; } }
    public static string NuGetExe_1_7 { get { return ourCachedNuGetExe_1_7.Value; } }
    public static string NuGetExe_1_8 { get { return ourCachedNuGetExe_1_8.Value; } }
    public static string NuGetExe_2_0 { get { return ourCachedNuGetExe_2_0.Value; } }
    public static string NuGetExe_2_1 { get { return ourCachedNuGetExe_2_1.Value; } }
    public static string NuGetExe_2_2 { get { return ourCachedNuGetExe_2_2.Value; } }
    public static string NuGetRunnerExe { get { return ourCachedNuGetRunnerPath.Value; } }

    public static string GetNuGetExe(NuGetVersion version)
    {
      switch (version)
      {
        case NuGetVersion.NuGet_1_4:
          return NuGetExe_1_4;
        case NuGetVersion.NuGet_1_5:
          return NuGetExe_1_5;
        case NuGetVersion.NuGet_1_6:
          return NuGetExe_1_6;
        case NuGetVersion.NuGet_1_7:
          return NuGetExe_1_7;
        case NuGetVersion.NuGet_1_8:
          return NuGetExe_1_8;
        case NuGetVersion.NuGet_2_0:
          return NuGetExe_2_0;
        case NuGetVersion.NuGet_2_1:
          return NuGetExe_2_1;
        case NuGetVersion.NuGet_2_2:
          return NuGetExe_2_2;


        case NuGetVersion.NuGet_2_2_CI:
          return ourCachedNuGet_CI_2_2.Value;
        case NuGetVersion.NuGet_Latest_CI:
          //timebomb
          if (DateTime.Now < new DateTime(2013, 2, 14)) throw new IgnoreException("Needs to be fixed sool");
          return ourCachedNuGet_CI_Last.Value;
        case NuGetVersion.NuGet_CommandLine_Package_Latest:
          return ourCachedNuGet_CommandLinePackage_Last.Value;
        default:
          throw new Exception("Unsupported nuget version: " + version);
      }
    }

    private static string FetchLatestNuGetPackage(string bt)
    {
      try
      {
        var homePath = CreateTempPath();
        string url = "http://ci.nuget.org:8080/guestAuth/repository/download/" + bt +
                     "/.lastSuccessful/Console/NuGet.exe";
        var nugetPath = Path.Combine(homePath, "NuGet.exe");
        var cli = new WebClient();
        cli.DownloadFile(url, nugetPath);
        return nugetPath;
      } catch(Exception e)
      {
        string message = "Failed to fetch NuGet build: " + bt;
        Assert.Ignore(message);
        Console.Out.WriteLine(e);
        throw new IgnoreException(message);
      }
    }

    private static string CreateTempPath()
    {
      var homePath = Path.GetTempFileName();
      File.Delete(homePath);
      Directory.CreateDirectory(homePath);
      return homePath;
    }

    private static string FetchLatestNuGetCommandline()
    {
      var temp = CreateTempPath();
      ProcessExecutor.ExecuteProcess(NuGetExe_2_2, "install", "NuGet.commandline", "-Source", NuGetConstants.NuGetFeed, "-ExcludeVersion", "-OutputDirectory",
                                     temp).Dump().AssertNoErrorOutput().AssertExitedSuccessfully();
      string nugetPath = Path.Combine(temp, "NuGet.CommandLine/tools/NuGet.Exe");
      Assert.IsTrue(File.Exists(nugetPath));
      return nugetPath;
    }


    public static NuGetVersion[] NuGetVersions
    {
      get { return AllNuGets().ToArray(); }
    }

    private static IEnumerable<NuGetVersion> AllNuGets()
    {
      return Enum.GetValues(typeof (NuGetVersion)).Cast<NuGetVersion>();
    }

    public static NuGetVersion[] NuGetVersions15p
    {
      get { return AllNuGets().Where(x => x >= NuGetVersion.NuGet_1_5).ToArray(); }
    }

    public static NuGetVersion[] NuGetVersions16p
    {
      get { return AllNuGets().Where(x => x >= NuGetVersion.NuGet_1_6).ToArray(); }
    }

    public static NuGetVersion[] NuGetVersions18p
    {
      get { return AllNuGets().Where(x => x >= NuGetVersion.NuGet_1_8).ToArray(); }
    }

    public static NuGetVersion[] NuGetVersions20p
    {
      get { return AllNuGets().Where(x => x >= NuGetVersion.NuGet_2_0).ToArray(); }
    }

  }

  public enum NuGetVersion
  {
    NuGet_1_4 = 4,
    NuGet_1_5 = 5,
    NuGet_1_6 = 6,
    NuGet_1_7 = 7,
    NuGet_1_8 = 8,
    NuGet_2_0 = 9,
    NuGet_2_1 = 10,
    NuGet_2_2 = 11,
    
    
    NuGet_2_2_CI = 988,
    NuGet_Latest_CI = 990,
    NuGet_CommandLine_Package_Latest = 999
  }


  public static class NuGetVersionExtensions
  {
    public static bool Is_1_4(this NuGetVersion version)
    {
      return version == NuGetVersion.NuGet_1_4;
    }
  }

}