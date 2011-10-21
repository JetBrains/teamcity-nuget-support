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
    public void DeserializePackage()
    {
      var x = new PackageLoader().Load(
        new ServiceMessageParser()
          .ParseServiceMessages(
            @"##teamcity[package Id='CommonServiceLocator' Version='1.0' Authors='Microsoft' Description='The Common Service Locator library contains a shared interface for service location which application and framework developers can reference. The library provides an abstraction over IoC containers and service locators. Using the library allows an application to indirectly access the capabilities without relying on hard references. The hope is that using this library, third-party applications and frameworks can begin to leverage IoC/Service Location without tying themselves down to a specific implementation.' IsLatestVersion='true' LastUpdated='2011-10-21T16:34:09Z' LicenseUrl='http://commonservicelocator.codeplex.com/license' PackageHash='RJjv0yxm+Fk/ak/CVMTGr0ng7g/nudkVYos4eQrIDpth3BdE1j7J2ddRm8FXtOoIZbgDqTU6hKq5zoackwL3HQ==' PackageHashAlgorithm='SHA512' PackageSize='37216' ProjectUrl='http://commonservicelocator.codeplex.com/' RequireLicenseAcceptance='false' TeamCityBuildId='42' TeamCityDownloadUrl='/repository/download/bt/42:id/null']"
          ).Single()
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