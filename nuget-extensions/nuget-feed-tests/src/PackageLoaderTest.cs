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