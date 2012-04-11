using System;
using System.IO;
using System.Linq;
using System.Xml;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListPackagesCommandTest
  {
    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_17_CI)]
    [TestCase(NuGetVersion.NuGet_18_CI)]
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
                                       <package source='" + NuGetConstants.DefaultFeedUrl_v1 + @"' id='NUnit' />
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
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_17_CI)]
    [TestCase(NuGetVersion.NuGet_18_CI)]
    public void TestCommand_ListPublic_Multiple(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
        fileOut =>
        TempFilesHolder.WithTempFile(
          fileIn =>
            {
              File.WriteAllText(fileIn,
                                @"<nuget-packages>
                                    <packages> "  +
                                                  string.Join("\n ", new[]{"NUnit", "YouTrackSharp", "Machine.Specifications", "jquery", "ninject"}.Select(x=>"<package source='" + NuGetConstants.DefaultFeedUrl_v1 + @"' id='" + x + "' />")) 
                                                  + @"
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
              Assert.True(doc.SelectNodes("//package[@id='jquery']//package-entry").Count > 0);
            }));
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_17_CI)]
    [TestCase(NuGetVersion.NuGet_18_CI)]
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
                                NuGetConstants.DefaultFeedUrl_v1 +
                                @"' id='NUnit' />
                                       <package source='" +
                                NuGetConstants.DefaultFeedUrl_v1 +
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
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_17_CI)]
    [TestCase(NuGetVersion.NuGet_18_CI)]
    public void TestCommand_ListPublicVersions_v1(NuGetVersion version)
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
                                NuGetConstants.DefaultFeedUrl_v1 +
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

    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_17_CI)]
    [TestCase(NuGetVersion.NuGet_18_CI)]
    public void TestCommand_ListPublicVersions_v2(NuGetVersion version)
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
                                NuGetConstants.DefaultFeedUrl_v2 +
                                @"' id='NUnit' versions='(1.1.1,2.5.8]'/>
                                    </packages>
                                   </nuget-packages>");

              ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                             "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
                .Dump()
                .AssertExitedSuccessfully()
                ;

              var text = File.ReadAllText(fileOut);
              Console.Out.WriteLine(text);
              Assert.False(text.Contains("version=\"2.5.10"));

              Console.Out.WriteLine("Result: " + text);
            }));
    }


    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    [TestCase(NuGetVersion.NuGet_17_CI)]
    [TestCase(NuGetVersion.NuGet_18_CI)]
    public void TestCommand_TeamListPublic_Local(NuGetVersion version)
    {
      TempFilesHolder.WithTempFile(
       fileOut =>
       TempFilesHolder.WithTempFile(
         fileIn =>
         {
           File.WriteAllText(fileIn,
                             @"<nuget-packages>
                                    <packages>
                                       <package source='" + Files.GetLocalFeed(version) + @"' id='Web'/>
                                    </packages>
                                   </nuget-packages>");

           ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                          "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
             .Dump()
             .AssertExitedSuccessfully()
             ;

           var text = File.ReadAllText(fileOut);
           Console.Out.WriteLine("Result: " + text);

           Assert.True(text.Contains("version=\"2.2.2"));
           Assert.True(text.Contains("version=\"1.1.1"));           
         }));
    }
  }
}