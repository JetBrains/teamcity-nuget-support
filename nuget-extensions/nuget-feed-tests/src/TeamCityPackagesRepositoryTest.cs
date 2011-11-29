using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.NuGet.PackageMetadataLoader;
using JetBrains.TeamCity.NuGet.Tests;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  [TestFixture]
  public class TeamCityPackagesRepositoryTest
  {
    private MemoryRepo myRepo;

    [SetUp]
    public void SetUp()
    {
      myRepo = new MemoryRepo();
    }

    [Test]
    public void EmptyRepo()
    {
      Assert.IsTrue(!myRepo.GetAllPackages().Any());
      Assert.AreEqual(0, myRepo.FilterById(new string[0]).Count());
    }

    [Test]
    public void test_IndexesOnePaclage()
    {
      var pkg = LoadPackage(Files.WebPackage_1_1_1);
      myRepo.AddSpec(pkg);

      Assert.AreEqual(1, myRepo.GetAllPackages().Count());
      Assert.AreEqual(1, myRepo.FilterById(new[] {"web"}).Count());
      Assert.AreEqual(1, myRepo.FilterById(new[] {"wEb"}).Count());
      
      Assert.AreEqual(0, myRepo.FilterById(new string[0]).Count());
      Assert.AreEqual(0, myRepo.FilterById(new[] {"zEb"}).Count());
    }

    private static TeamCityPackage LoadPackage(string package)
    {
      var spec = new TeamCityPackageSpec
                   {
                     BuildId = 777,
                     BuildType = "bt10",
                     DownloadUrl = "tbd",
                     PackageFile = package
                   };
      return TeamCityZipPackageFactory.LoadPackage(package, spec);
      
    }
  }
}