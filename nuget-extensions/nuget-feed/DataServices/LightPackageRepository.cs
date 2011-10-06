using System.Linq;
using NuGet;
using NuGet.Server.DataServices;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class LightPackageRepository
  {
    protected TeamCityZipPackage OpenPackage(string path)
    {
      return new TeamCityZipPackage(path);
    }

    public IQueryable<Package> GetPackages()
    {
      return (new Package[0]).AsQueryable();
    }

    public Package GetMetadataPackage(IPackage package)
    {
      return ((TeamCityZipPackage) package).ToPackage;
    }
  }
}