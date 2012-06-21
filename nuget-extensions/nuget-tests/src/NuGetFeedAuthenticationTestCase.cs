using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public class NuGetFeedAuthenticationTestCase  : NuGetSettingsBackupingTestCase
  {

    [Serializable, XmlRoot("teamcity-feeds-list")]
    public class AuthenticatedFeedsList
    {
      [XmlArray("feeds"), XmlArrayItem("feed")]
      public AuthenticatedFeed[] Feeds { get; set; }
    }

    [Serializable, XmlRoot("feed")]
    public class AuthenticatedFeed
    {
      [XmlAttribute("url")]
      public string Url { get; set; }
      [XmlAttribute("user")]
      public string UserName { get; set; }
      [XmlAttribute("password")]
      public string Password { get; set; }
    }

    protected static AuthenticatedFeed f(string url, string user = null, string password = null)
    {
      return new AuthenticatedFeed
        {
          Url = url,
          UserName = user,
          Password = password
        };
    }


    protected static string DoTestSetUrl(NuGetVersion version, params AuthenticatedFeed[] pp)
    {
      return TempFilesHolder.WithTempFile(
        fileIn =>
          {
            fileIn.SaveAsXml(new AuthenticatedFeedsList { Feeds = pp });
            return ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                                  "TeamCity.AuthorizeFeed", "-Request", fileIn)
              .Dump()
              .AssertExitedSuccessfully()
              .Output
              ;
          });
    }

    protected static IEnumerable<AuthenticatedFeed> DoTestReadUrl(NuGetVersion version)
    {
      return TempFilesHolder.WithTempFile(
        fileOut =>
          {
            ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                           "TeamCity.DumpFeed", "-Response", fileOut)
              .Dump()
              .AssertExitedSuccessfully()
              ;

            return fileOut.LoadAsXml<AuthenticatedFeedsList>().Feeds;
          });
    }

  }
}