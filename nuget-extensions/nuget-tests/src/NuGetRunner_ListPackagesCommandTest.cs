using System;
using System.Linq;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetRunner_ListPackagesCommandTest : NuGetRunner_ListPackagesCommandTestBase
  {
    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_ListPublic(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p1("Microsoft.Build")));
      var mpdes = PackagesCount(doc, "Microsoft.Build");
      
      if (version > NuGetVersion.NuGet_1_4)
        Assert.True(mpdes == 1);
      else
        Assert.True(mpdes > 0);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions15p")]
    public void TestCommand_ListPublic_Multiple(NuGetVersion version)
    {
      var doc = DoTestWithSpec(
        version,
        Serialize(
          new[] { "Microsoft.Build", "Microsoft.Build.Engine", "Microsoft.Build.Runtime", "jquery", "ninject"}
            .Select(x => p1(x))));
      
      Assert.True(PackagesCount(doc, "Microsoft.Build") == 1);
      Assert.True(PackagesCount(doc, "Microsoft.Build.Engine") == 1);
      Assert.True(PackagesCount(doc, "Microsoft.Build.Runtime") == 1);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions58p"), Ignore]
    public void TestCommand_ListPublic_Multiple_V3(NuGetVersion version)
    {
      var doc = DoTestWithSpec(
        version,
        Serialize(
          new[] { "Microsoft.Build", "Microsoft.Build.Engine", "Microsoft.Build.Runtime", "jquery", "ninject" }
            .Select(x => p3(x))));

      Assert.True(PackagesCount(doc, "Microsoft.Build") == 1);
      Assert.True(PackagesCount(doc, "Microsoft.Build.Engine") == 1);
      Assert.True(PackagesCount(doc, "Microsoft.Build.Runtime") == 1);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_ListPublic_Multiple_sameIds(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p1("Microsoft.Build"), p1("Microsoft.Build", "[15.1.548,15.4.8]")));

      var notVersioned = doc.SelectNodes("//package[@id='Microsoft.Build' and not(@versions='[15.1.548,15.4.8]')]//package-entry").Count;
      var versioned = doc.SelectNodes("//package[@id='Microsoft.Build' and @versions='[15.1.548,15.4.8]']//package-entry").Count;

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

    [Test, TestCaseSource(typeof(Files), "NuGetVersions58p"), Ignore]
    public void TestCommand_ListPublic_Multiple_sameIds_V3(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p3("Microsoft.Build"), p1("Microsoft.Build", "[15.1.548,15.4.8]")));

      var notVersioned = doc.SelectNodes("//package[@id='Microsoft.Build' and not(@versions='[15.1.548,15.4.8]')]//package-entry").Count;
      var versioned = doc.SelectNodes("//package[@id='Microsoft.Build' and @versions='[15.1.548,15.4.8]']//package-entry").Count;

      Assert.True(versioned > 0);
      Assert.True(notVersioned > 0);
      Assert.True(notVersioned == 1);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_ListPublicVersions_v1(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p1("Microsoft.Build", "(15.1.548,15.4.8]")));
      Assert.False(doc.OuterXml.Contains("version=\"15.5.179"));
      Console.Out.WriteLine("Result: " + doc.OuterXml);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions16p")]
    public void TestCommand_ListPublicVersions_v2(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p2("Microsoft.Build", "(15.1.548,15.4.8]")));
      Assert.False(doc.OuterXml.Contains("version=\"15.1.548"));
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions58p"), Ignore]
    public void TestCommand_ListPublicVersions_v3(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p3("Microsoft.Build", "(15.1.548,15.4.8]")));
      Assert.False(doc.OuterXml.Contains("version=\"15.1.548"));
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_TeamListPublic_Local(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p(Files.GetLocalFeed(version), "Web"))).OuterXml;
      Assert.True(doc.Contains("version=\"2.2.2"));
      Assert.True(doc.Contains("version=\"1.1.1"));      
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_TeamListPublic_Local_URI(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p(Files.GetLocalFeedURI(version), "Web"))).OuterXml;
      Assert.True(doc.Contains("version=\"2.2.2"));
      Assert.True(doc.Contains("version=\"1.1.1"));      
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions")]
    public void TestCommand_TeamListPublic_Local_Unresolved(NuGetVersion version)
    {
      DoTestWithSpec(version, Serialize(p("%some%type_here", "Web")));
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions18p")]
    public void TestCommand_TeamListPublic_Local_Prerelease(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p(Files.GetLocalFeed_1_8(), "Web", includePrerelease: true))).OuterXml;
      Assert.True(doc.Contains("version=\"2.2.2"));
      Assert.True(doc.Contains("version=\"1.1.1"));      
      Assert.True(doc.Contains("version=\"4.0.1-beta"));      
      Assert.True(doc.Contains("version=\"2.2.2-rc"));      
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions18p")]
    public void TestCommand_TeamListPublic_Web_Prerelease_v2(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p2("CassiniDev", includePrerelease: true)));
      Assert.True(doc.OuterXml.Contains("version=\"5.0.2-"));

      Assert.IsTrue(PackagesCount(doc, "CassiniDev") == 1);
    }

    [Test, TestCaseSource(typeof(Files), "NuGetVersions58p"), Ignore]
    public void TestCommand_TeamListPublic_Web_Prerelease_v3(NuGetVersion version)
    {
      var doc = DoTestWithSpec(version, Serialize(p3("CassiniDev", includePrerelease: true)));
      Assert.True(doc.OuterXml.Contains("version=\"5.0.2-"));

      Assert.IsTrue(PackagesCount(doc, "CassiniDev") == 1);
    }
  }
}
