using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using NUnit.Framework;
using System.Linq;
using System.ServiceModel.Syndication;
using System.Text.RegularExpressions;
using System.Threading;
using System.Xml;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class Files
  {
    private static readonly Lazy<string> ourCachedNuGetExe_1_4 = PathSearcher.SearchFile("packages/NuGet.CommandLine.1.4.20615.182/tools/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_5 = PathSearcher.SearchFile("packages/NuGet.CommandLine.1.5.21005.9019/tools/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_6 = PathSearcher.SearchFile("packages/NuGet.CommandLine.1.6.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_7 = PathSearcher.SearchFile("packages/NuGet.CommandLine.1.7.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_1_8 = PathSearcher.SearchFile("packages/NuGet.CommandLine.1.8.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_0 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.0.40001/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_1 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.1.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_2 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.2.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_5 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.5.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_6 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.6.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_7 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.7.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_2_8 = PathSearcher.SearchFile("packages/NuGet.CommandLine.2.8.6/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_3_2 = PathSearcher.SearchFile("packages/NuGet.CommandLine.3.2.0/tools/nuget.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_3_3 = PathSearcher.SearchFile("packages/NuGet.CommandLine.3.3.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_3_4 = PathSearcher.SearchFile("packages/NuGet.CommandLine.3.4.4-rtm-final/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_3_5 = PathSearcher.SearchFile("packages/NuGet.CommandLine.3.5.0-rtm-1938/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_4_0 = PathSearcher.SearchFile("packages/NuGet.CommandLine.4.0.0-rtm-2283/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_4_1 = PathSearcher.SearchFile("packages/NuGet.CommandLine.4.1.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_4_3 = PathSearcher.SearchFile("packages/NuGet.CommandLine.4.3.0/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetExe_4_8 = PathSearcher.SearchFile("packages/NuGet.CommandLine.4.8.0*/tools/NuGet.exe");
    private static readonly Lazy<string> ourCachedNuGetRunnerPath = PathSearcher.SearchFile("JetBrains.TeamCity.NuGetRunner.exe", "bin/JetBrains.TeamCity.NuGetRunner.exe");
    private static readonly Lazy<string> ourLocalFeed = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed");
    private static readonly Lazy<string> ourLocalFeed_1_4 = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed_1.4");
    private static readonly Lazy<string> ourLocalFeed_1_8 = PathSearcher.SearchDirectory("nuget-tests/testData/localFeed_1.8");    
    private static readonly Lazy<string> ourCachedNuGet_CommandLinePackage_Last = new Lazy<string>(FetchLatestNuGetCommandline, LazyThreadSafetyMode.PublicationOnly);
    
    // Example: https://dotnet.myget.org/F/nuget-build/api/v2/Packages(Id='NuGet.CommandLine',Version='3.2.0')
    private static readonly Regex Version = new Regex("Version='([^']+)'", RegexOptions.Compiled);

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
    public static string NuGetExe_2_5 { get { return ourCachedNuGetExe_2_5.Value; } }
    public static string NuGetExe_2_6 { get { return ourCachedNuGetExe_2_6.Value; } }
    public static string NuGetExe_2_7 { get { return ourCachedNuGetExe_2_7.Value; } }
    public static string NuGetExe_2_8 { get { return ourCachedNuGetExe_2_8.Value; } }
    public static string NuGetExe_3_2 { get { return ourCachedNuGetExe_3_2.Value; } }
    public static string NuGetExe_3_3 { get { return ourCachedNuGetExe_3_3.Value; } }
    public static string NuGetExe_3_4 { get { return ourCachedNuGetExe_3_4.Value; } }
    public static string NuGetExe_3_5 { get { return ourCachedNuGetExe_3_5.Value; } }
    public static string NuGetExe_4_0 { get { return ourCachedNuGetExe_4_0.Value; } }
    public static string NuGetExe_4_1 { get { return ourCachedNuGetExe_4_1.Value; } }
    public static string NuGetExe_4_3 { get { return ourCachedNuGetExe_4_3.Value; } }
    public static string NuGetExe_4_8 { get { return ourCachedNuGetExe_4_8.Value; } }
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
        case NuGetVersion.NuGet_2_5:
          return NuGetExe_2_5;
        case NuGetVersion.NuGet_2_6:
          return NuGetExe_2_6;
        case NuGetVersion.NuGet_2_7:
          return NuGetExe_2_7;
        case NuGetVersion.NuGet_2_8:
          return NuGetExe_2_8;
        case NuGetVersion.NuGet_3_2:
          return NuGetExe_3_2;
        case NuGetVersion.NuGet_3_3:
          return NuGetExe_3_3;
        case NuGetVersion.NuGet_3_4:
          return NuGetExe_3_4;
        case NuGetVersion.NuGet_3_5:
          return NuGetExe_3_5;
        case NuGetVersion.NuGet_4_0:
          return NuGetExe_4_0;
        case NuGetVersion.NuGet_4_1:
          return NuGetExe_4_1;
        case NuGetVersion.NuGet_4_3:
          return NuGetExe_4_3;
        case NuGetVersion.NuGet_CommandLine_Package_Latest:
            return ourCachedNuGet_CommandLinePackage_Last.Value;
        default:
          throw new Exception("Unsupported nuget version: " + version);
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
      var version = GetLatestRtmVersion();
      var temp = CreateTempPath();
      ProcessExecutor.Result result = null;
      
      for (int i = 2; i > 0; i--)
      {
        result = ProcessExecutor.ExecuteProcess(NuGetExe_4_3, "install", "NuGet.commandline",
            "-Source", NuGetConstants.DefaultFeedUrl_v2,
            "-Source", NuGetConstants.NuGetDevFeed,
            "-Version", version,
            "-ExcludeVersion", "-OutputDirectory", temp,
            "-Verbosity", "detailed")
          .Dump().AssertNoErrorOutput();
        if (result.ExitCode == 0) break;
      }

      Assert.NotNull(result, "Result is null");
      result.AssertExitedSuccessfully();
      
      string nugetPath = Path.Combine(temp, "NuGet.CommandLine/tools/NuGet.exe");
      Assert.IsTrue(File.Exists(nugetPath), string.Format("File {0} does not exists", nugetPath));
      return nugetPath;
    }

    private static string GetLatestRtmVersion()
    {
      String version = "4.3.0";
      
      try
      {
        var url = "https://dotnet.myget.org/F/nuget-build/api/v2/FindPackagesById?id='NuGet.CommandLine'&$skiptoken='NuGet.CommandLine','4.5.0-rtm-4681'";
        do
        {
          using (var reader = XmlReader.Create(url))
          {
            var feed = SyndicationFeed.Load(reader);
            foreach (var package in feed.Items)
            {
              var id = package.Id;
              if (String.IsNullOrEmpty(id)) continue;

              var match = Version.Match(id);
              if (match.Success)
              {
                var value = match.Groups[1].Value;
                if (value.Contains("rtm"))
                {
                  if (String.Compare(value, version, StringComparison.Ordinal) > 0)
                  {
                    version = value;
                  }
                }
              }
            }
            var nextLink = feed.Links.FirstOrDefault(p => p.RelationshipType == "next");
            url = nextLink != null ? nextLink.Uri.ToString() : null;
          }
        } while (!string.IsNullOrEmpty(url));
      }
      catch (Exception e)
      {
        Console.WriteLine(e);
      }

      return version;
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
    NuGet_2_5 = 12,
    NuGet_2_6 = 13,
    NuGet_2_7 = 14,
    NuGet_2_8 = 15,
    NuGet_3_2 = 16,
    NuGet_3_3 = 17,
    NuGet_3_4 = 18,
    NuGet_3_5 = 19,
    NuGet_4_0 = 20,
    NuGet_4_1 = 21,
    NuGet_4_3 = 22,


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
