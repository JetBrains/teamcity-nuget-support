using System.Linq;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public class NuGetFeedAuthenticationTest : NuGetFeedAuthenticationTestCase
  {
    [TestCase(NuGetVersion.NuGet_2_0)]
    [TestCase(NuGetVersion.NuGet_2_1_CI)]
    [TestCase(NuGetVersion.NuGet_CommandLine_Package_Latest)]
    [TestCase(NuGetVersion.NuGet_Latest_CI)]
    public void TestSetUrl(NuGetVersion version)
    {
      DoTestSetUrl(version, f("http://localhost:8111", "jonny", "super"));
      var p = DoTestReadUrl(version);
      
      Assert.That(p.Any(x=>x.UserName == "jonny" && x.Password == "super" && x.Url == "http://localhost:8111"), Is.True);
    }
  }
}