using System.IO;
using JetBrains.TeamCity.NuGet.Feed.DataServices;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  [TestFixture]
  public class LightPackageRepositoryTest : LightPackageRepositoryBase
  {
    private int myPackageCount = 333;

    [Test]
    public void CreateEmptryRepoTest()
    {
      File.Delete(PackagesFile);
      Assert.IsFalse(Repo.GetPackages().Any());
      Assert.IsFalse(File.Exists(PackagesFile));
    }

    [Test]
    public void AddOnePackage()
    {
      var pkg = AddPackage();
      Assert.AreEqual(1, Repo.GetPackages().Count());

      Assert.IsTrue(File.Exists(PackagesFile));
      Assert.IsTrue(Repo.GetPackages().Any(x=>x.Id == pkg.Package.Id));
    }

    private TeamCityPackageEntry AddPackage()
    {
      var pkg = (++myPackageCount).ToEntry();
      Repo.RegisterPackage(pkg);
      return pkg;
    }

    [Test]
    public void AddOnePackagePersisted()
    {
      var pkg = AddPackage();
      RecreateRepo();
      
      Assert.AreEqual(1, Repo.GetPackages().Count());
      Assert.IsTrue(Repo.GetPackages().Any(x => x.Id == pkg.Package.Id));
    }

    [Test]
    public void RememberAddedPackages()
    {
      10.Iterate(() => AddPackage());
      Assert.AreEqual(10, Repo.GetPackages().Count());

      RecreateRepo();
      Assert.AreEqual(10, Repo.GetPackages().Count());
    }

    [Test]
    public void ShouldRereadPackagesOnRecreate()
    {
      10.Iterate(() => AddPackage());
      Assert.AreEqual(10, Repo.GetPackages().Count());

      File.Delete(PackagesFile);
      RecreateRepo();
      
      Assert.AreEqual(0, Repo.GetPackages().Count());
    }


    [Test]
    public void ThereShouldBeLatestPackage()
    {
      AddPackage();
      Assert.IsTrue(Repo.GetPackages().Any(x => x.IsLatestVersion));
      RecreateRepo();
      Assert.IsTrue(Repo.GetPackages().Any(x => x.IsLatestVersion));
    }

    [Test]
    public void CleanupShouldRemovePackagesWithNoFiles()
    {      
      var noFile = AddPackage();
      var withFile = AddPackage();
      File.WriteAllText(withFile.GetPackagePath(this), "this is an awesome package");
      
      Assert.AreEqual(2, Repo.GetPackages().Count());
      
      Repo.CleanupObsoletePackages();

      Assert.AreEqual(1, Repo.GetPackages().Count());
      Assert.IsTrue(Repo.GetPackages().Where(x=>x.Id == withFile.Package.Id).Any());

      RecreateRepo();

      Assert.AreEqual(1, Repo.GetPackages().Count());
      Assert.IsTrue(Repo.GetPackages().Where(x => x.Id == withFile.Package.Id).Any());
    }

    [Test]
    public void PackageAddShouldUpdateIsLatest()
    {
      var pkg1 = AddPackage();
      var pkg2 = Clone(pkg1);
      pkg2.Package.Version = "2.2.2.2";

      Repo.RegisterPackage(pkg2);

      Assert.AreEqual(1, Repo.GetPackages().Where(x=>x.IsLatestVersion).Count());
      Assert.AreEqual(1, Repo.GetPackages().Where(x=>!x.IsLatestVersion).Count());

      Assert.IsTrue(Repo.GetPackages().Where(x=>x.IsLatestVersion).Where(x=>x.Version == "2.2.2.2").Any());

      RecreateRepo();

      Assert.AreEqual(1, Repo.GetPackages().Where(x => x.IsLatestVersion).Count());
      Assert.AreEqual(1, Repo.GetPackages().Where(x => !x.IsLatestVersion).Count());
    }

    [Test]
    public void SouldSupportAddRemove()
    {
      int sz = 10;
      var packages = sz.Repeat(AddPackage).ToList();

      foreach (var p in packages)
      {
        File.WriteAllText(p.GetPackagePath(this), "aaa");
      }

      foreach (var p in packages)
      {
        RecreateRepo();
        Repo.CleanupObsoletePackages();
        Assert.AreEqual(sz--, Repo.GetPackages().Count());

        File.Delete(p.GetPackagePath(this));
      }

      RecreateRepo();
      Repo.CleanupObsoletePackages();
      Assert.AreEqual(0, Repo.GetPackages().Count());
    }

  }
}