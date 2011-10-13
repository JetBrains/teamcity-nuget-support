using System.IO;
using System.Text;
using System.Xml.Serialization;
using JetBrains.TeamCity.NuGet.Feed.DataServices;
using JetBrains.TeamCity.NuGet.Tests;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Feed.Tests
{
  public class LightPackageRepositoryBase : IRepositorySettings 
  {
    private string myPackagesFile;
    private string myPackagesBase;
    private LightPackageRepository myRepo;

    [SetUp]
    public void SetUp()
    {
      myPackagesFile = Path.GetTempFileName();
      myPackagesBase = TempFilesHolder.CreateTempDirectory();
      RecreateRepo();
    }

    protected void RecreateRepo()
    {
      myRepo = new LightPackageRepository(this);
    }

    [TearDown]
    public void TearDown()
    {
      if (myPackagesFile != null) File.Delete(myPackagesFile);
      if (myPackagesBase != null) Directory.Delete(myPackagesBase, true);
    }

    public LightPackageRepository Repo
    {
      get { return myRepo; }
    }

    public string PackagesFile
    {
      get { return myPackagesFile; }
    }

    public string PackagesBase
    {
      get { return myPackagesBase; }
    }

    protected T Clone<T>(T t)
    {
      var xml = XmlSerializers<T>.Create();
      var ms = new MemoryStream();
      xml.Serialize(ms, t);
      return (T) xml.Deserialize(new MemoryStream(ms.GetBuffer()));
    }
  }
}