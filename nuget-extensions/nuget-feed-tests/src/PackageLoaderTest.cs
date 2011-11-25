using System;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.ServiceMessages.Read;
using JetBrains.TeamCity.ServiceMessages.Write;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  [TestFixture]
  public class PackageLoaderTest
  {
    public const string SMAPLE_PACKAGE =
      @"##teamcity[package Id='CommonServiceLocator' Version='1.0' Authors='Microsoft' Description='The Common Service Locator library contains a shared interface for service location which application and framework developers can reference. The library provides an abstraction over IoC containers and service locators. Using the library allows an application to indirectly access the capabilities without relying on hard references. The hope is that using this library, third-party applications and frameworks can begin to leverage IoC/Service Location without tying themselves down to a specific implementation.' IsLatestVersion='true' LastUpdated='2011-10-21T16:34:09Z' LicenseUrl='http://commonservicelocator.codeplex.com/license' PackageHash='RJjv0yxm+Fk/ak/CVMTGr0ng7g/nudkVYos4eQrIDpth3BdE1j7J2ddRm8FXtOoIZbgDqTU6hKq5zoackwL3HQ==' PackageHashAlgorithm='SHA512' PackageSize='37216' ProjectUrl='http://commonservicelocator.codeplex.com/' RequireLicenseAcceptance='false' TeamCityBuildId='42' TeamCityDownloadUrl='/repository/download/bt/42:id/null']";

    public const string SMAPLE_PACKAGE2 =
      @"##teamcity[package Id='CommonServiceLocator' Version='1.0' Authors='Microsoft' Description='The Common Service Locator library contains a shared interface for service location which application and framework developers can reference. The library provides an abstraction over IoC containers and service locators. Using the library allows an application to indirectly access the capabilities without relying on hard references. The hope is that using this library, third-party applications and frameworks can begin to leverage IoC/Service Location without tying themselves down to a specific implementation.' IsLatestVersion='true' LastUpdated='j1322223386258' LicenseUrl='http://commonservicelocator.codeplex.com/license' PackageHash='RJjv0yxm+Fk/ak/CVMTGr0ng7g/nudkVYos4eQrIDpth3BdE1j7J2ddRm8FXtOoIZbgDqTU6hKq5zoackwL3HQ==' PackageHashAlgorithm='SHA512' PackageSize='37216' ProjectUrl='http://commonservicelocator.codeplex.com/' RequireLicenseAcceptance='false' TeamCityBuildId='42' TeamCityDownloadUrl='/repository/download/bt/42:id/null']";

    [Test]
    public void SimpleFill()
    {
      var loader = new PackageLoader();
      var argz = new
                   {
                     Id = "Id", 
                     Version = "1.2.34.4Beta",
                     RequireLicenseAcceptance = true,
                     PackageSize = 500L,
                     IsLatestVersion = true,
                     LastUpdated = "2011-01-07T07:57:25.307",
                     TeamCityDownloadUrl = "url",
                     Authors = "authors",
                     PackageHash = "hash",
                     PackageHashAlgorithm = "SHA512",
                   };

      var x = loader.Load(
        new ServiceMessageParser()
          .ParseServiceMessages(
            new ServiceMessageFormatter()
              .FormatMessage("package", argz)
              ).Single()
        );
    }

    [Test, ExpectedException(typeof(RemoteException))]
    public void SimpleCheckParameters()
    {
      var loader = new PackageLoader();
      var argz = new
                   { 
                     AAA = "bbb"
                   };

      var x = loader.Load(
        new ServiceMessageParser()
          .ParseServiceMessages(
            new ServiceMessageFormatter()
              .FormatMessage("package", argz)
              ).Single()
        );
    }

    [Test]
    public void PackageLoadPerfomanceTest_ISO_date()
    {
      var msg = new ServiceMessageParser().ParseServiceMessages(SMAPLE_PACKAGE).Single();
      var loader = new PackageLoader();
      Action test =
        () =>
          {
            for (int i = 0; i < 500000; i++)
            {
              loader.Load(msg);
            }
          };

      test.ExpectTime(TimeSpan.FromMilliseconds(2500), 5);
    }

    [Test]
    public void PackageLoadPerfomanceTest_JAVA_date()
    {
      var msg = new ServiceMessageParser().ParseServiceMessages(SMAPLE_PACKAGE2).Single();
      var loader = new PackageLoader();
      Action test =
        () =>
          {
            for (int i = 0; i < 500000; i++)
            {
              loader.Load(msg);
            }
          };

      test.ExpectTime(TimeSpan.FromMilliseconds(2000), 5);
    }


    [Test]
    public void DeserializePackage()
    {
      var x = new PackageLoader().Load(
        new ServiceMessageParser().ParseServiceMessages(SMAPLE_PACKAGE).Single()
        );
    }

    [Test, Explicit]
    public void DumpRequiredPackageParameters()
    {
      foreach (var name in new PackageLoader().GenerateRequiredParameterNames())
      {
        Console.Out.WriteLine("parameters.put(\"{0}\", \"\");", name);
      }
    }

    [Test, Explicit]
    public void DumpRequiredPackageParameters2()
    {
      foreach (var name in new PackageLoader().GenerateRequiredParameterNames())
      {
        Console.Out.WriteLine("addParameter(map, \"{0}\", \"\");", name);
      }
    }
  }
}