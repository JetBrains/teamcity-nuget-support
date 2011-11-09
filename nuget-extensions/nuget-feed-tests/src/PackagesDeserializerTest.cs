using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.ServiceMessages.Read;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  [TestFixture, Explicit]
  public class PackagesDeserializerTest
  {
    private PackagesDeserializer myDes;

    [SetUp]
    public void SetUp()
    {
      myDes = new PackagesDeserializer(new ServiceMessageParser(), new PackageLoader());
    }

    [Test]
    public void PackageLoaderPerformanceTest_5000()
    {
      var data = GenerateTestData(5000);
      var trash = new ArrayList();
      MeasureTime(TimeSpan.FromMilliseconds(500), 10, () => trash.Add(myDes.ProcessPackages(new StringReader(data)).ToArray()));
      Console.Out.WriteLine(trash.Count);
    }

    private static string GenerateTestData(int sz)
    {
      var sb = new StringBuilder();
      while (sz-- > 0)
      {
        sb.AppendFormat("##teamcity[package Id='CommonServiceLocator{0}' Version='1.0.{0}' IsLatestVersion='true' teamcity.artifactPath='some/package/download/CommonServiceLocator.1.0.{0}.nupkg' Authors='Microsoft' Description='The Common Service Locator library contains a shared interface for service location which application and framework developers can reference. The library provides an abstraction over IoC containers and service locators. Using the library allows an application to indirectly access the capabilities without relying on hard references. The hope is that using this library, third-party applications and frameworks can begin to leverage IoC/Service Location without tying themselves down to a specific implementation.'  LastUpdated='2011-10-21T16:34:09Z' LicenseUrl='http://commonservicelocator.codeplex.com/license' PackageHash='RJjv0yxm+Fk/ak/CVMTGr0ng7g/nudkVYos4eQrIDpth3BdE1j7J2ddRm8FXtOoIZbgDqTU6hKq5zoackwL3HQ==' PackageHashAlgorithm='SHA512' PackageSize='37216' ProjectUrl='http://commonservicelocator.codeplex.com/' RequireLicenseAcceptance='false' TeamCityBuildId='42' TeamCityDownloadUrl='/app/nuget-packages/jonnyzzz5Z8mBocMOdtH4CJhxRaev11WxcWpHVCrrulezz/42/some/package/download/CommonServiceLocator.1.0.nupkg']", sz);
        sb.AppendLine();
      }
      return sb.ToString();
    }


    private static void MeasureTime(TimeSpan time, int repeat, Action action)
    {
      var result = new List<TimeSpan>();
      while (repeat-- > 0)
      {
        var start = DateTime.Now;
        action();
        var span = DateTime.Now - start;

        Console.Out.WriteLine("Action finished in: {0}", span.TotalMilliseconds);

        if (span < time) return;
        result.Add(span);
      }

      Assert.Fail("Action is expected to complete in {0}, but was [{1}]", time.TotalMilliseconds, String.Join(", ", result.Select(x => x.TotalMilliseconds.ToString()).ToArray()));
    }
  }
}