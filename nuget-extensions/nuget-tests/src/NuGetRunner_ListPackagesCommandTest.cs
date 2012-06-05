using System;
using System.Linq;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListPackagesCommandTest : NuGetRunner_ListPackagesCommandTestBase
  {
    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_1_8)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublic(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p1("NUnit")));
      var mpdes = PackagesCount(doc, "NUnit");
      
      if (version > NuGetVersion.NuGet_1_4)
        Assert.True(mpdes == 1);
      else
        Assert.True(mpdes > 0);
    }

    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_1_8)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublic_Multiple(NuGetVersion version)
    {
      var doc = DoTestWithSpec(
        version,
        Serialize(
          new[] {"NUnit", "YouTrackSharp", "Machine.Specifications", "jquery", "ninject"}
            .Select(x => p1(x))));
      
      Assert.True(PackagesCount(doc, "NUnit") == 1);
      Assert.True(PackagesCount(doc, "YouTrackSharp") == 1);
      Assert.True(PackagesCount(doc, "jquery") == 1);
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_1_8)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublic_Multiple_sameIds(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p1("NUnit"), p1("NUnit", "(1.1.1,2.5.8]")));

      var notVersioned = doc.SelectNodes("//package[@id='NUnit' and not(@versions='(1.1.1,2.5.8]')]//package-entry").Count;
      var versioned = doc.SelectNodes("//package[@id='NUnit' and @versions='(1.1.1,2.5.8]']//package-entry").Count;

      Assert.True(versioned > 0);
      Assert.True(notVersioned > 0);

      if (version > NuGetVersion.NuGet_1_4)
      {
        Assert.True(notVersioned == 1);        
      }
      else
      {
        Assert.True(versioned < notVersioned);
      }
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_1_8)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublicVersions_v1(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p1("NUnit", "(1.1.1,2.5.8]")));
      Assert.False(doc.OuterXml.Contains("version=\"2.5.10"));
      Console.Out.WriteLine("Result: " + doc.OuterXml);
    }

    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_1_8)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_ListPublicVersions_v2(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p2("NUnit", "(1.1.1,2.5.8]")));
      Assert.False(doc.OuterXml.Contains("version=\"2.5.10"));
    }

    [TestCase(NuGetVersion.NuGet_1_4)]
    [TestCase(NuGetVersion.NuGet_1_5)]
    [TestCase(NuGetVersion.NuGet_1_6)]
    [TestCase(NuGetVersion.NuGet_1_7)]
    [TestCase(NuGetVersion.NuGet_1_8)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestCommand_TeamListPublic_Local(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p(Files.GetLocalFeed(version), "Web"))).OuterXml;
      Assert.True(doc.Contains("version=\"2.2.2"));
      Assert.True(doc.Contains("version=\"1.1.1"));      
    }
  }
}