using System;
using System.IO;
using System.Xml;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListPackagesCommandTest
  {
    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublic(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
        fileOut =>
        TempFilesHolder.WithTempFile(
          fileIn =>
            {
              File.WriteAllText(fileIn,
                                @"<nuget-packages>
                                    <packages>
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl +
                                @"' id='NUnit' />
                                    </packages>
                                   </nuget-packages>");

              ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                             "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
                .Dump()
                .AssertExitedSuccessfully()
                ;

              Console.Out.WriteLine("Result: " + File.ReadAllText(fileOut));

              var doc = new XmlDocument();
              doc.Load(fileOut);
              Assert.True(doc.SelectNodes("//package[@id='NUnit']//package-entry").Count > 0);
            }));
    }


    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublic_Multiple(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
        fileOut =>
        TempFilesHolder.WithTempFile(
          fileIn =>
            {
              File.WriteAllText(fileIn,
                                @"<nuget-packages>
                                    <packages>
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl +
                                @"' id='NUnit' />
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl +
                                @"' id='YouTrackSharp' />
                                    </packages>
                                   </nuget-packages>");

              ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                             "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
                .Dump()
                .AssertExitedSuccessfully()
                ;

              Console.Out.WriteLine("Result: " + File.ReadAllText(fileOut));

              var doc = new XmlDocument();
              doc.Load(fileOut);
              Assert.True(doc.SelectNodes("//package[@id='NUnit']//package-entry").Count > 0);
              Assert.True(doc.SelectNodes("//package[@id='YouTrackSharp']//package-entry").Count > 0);
            }));
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublic_Multiple_sameIds(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
        fileOut =>
        TempFilesHolder.WithTempFile(
          fileIn =>
            {
              File.WriteAllText(fileIn,
                                @"<nuget-packages>
                                    <packages>
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl +
                                @"' id='NUnit' />
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl +
                                @"' id='NUnit' versions='(1.1.1,2.5.8]' />
                                    </packages>
                                   </nuget-packages>");

              ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                             "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
                .Dump()
                .AssertExitedSuccessfully()
                ;

              Console.Out.WriteLine("Result: " + File.ReadAllText(fileOut));

              var doc = new XmlDocument();
              doc.Load(fileOut);
              Assert.True(doc.SelectNodes("//package[@id='NUnit']//package-entry").Count > 0);
              Assert.True(doc.SelectNodes("//package[@id='NUnit' and @versions='(1.1.1,2.5.8]']//package-entry").Count < doc.SelectNodes("//package[@id='NUnit' and not(@versions='(1.1.1,2.5.8]')]//package-entry").Count);


            }));
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublicVersions(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
        fileOut =>
        TempFilesHolder.WithTempFile(
          fileIn =>
            {
              File.WriteAllText(fileIn,
                                @"<nuget-packages>
                                    <packages>
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl +
                                @"' id='NUnit' versions='(1.1.1,2.5.8]'/>
                                    </packages>
                                   </nuget-packages>");

              ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                             "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
                .Dump()
                .AssertExitedSuccessfully()
                ;

              var text = File.ReadAllText(fileOut);
              Assert.False(text.Contains("version=\"2.5.10"));

              Console.Out.WriteLine("Result: " + text);
            }));
    }
  }
}