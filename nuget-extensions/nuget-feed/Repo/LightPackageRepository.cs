using System;
using System.IO;
using System.Linq;
using System.Web.Configuration;
using System.Web.Hosting;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class LightPackageRepository
  {
    private static readonly object CONFIG_ACCESS_LOG = new object();

    private readonly Lazy<PackageStore> myStore;

    public LightPackageRepository()
    {
      myStore = new Lazy<PackageStore>(() => new PackageStore(TeamCityPackagesFile));
    }

    public IQueryable<TeamCityPackage> GetPackages()
    {
      return new PackagesQueryProvider(myStore.Value).Query;
    }

    public void RegisterPackage(TeamCityPackageSpec spec)
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var pkg = TeamCityZipPackageFactory.LoadPackage(spec.PackageFile, spec);
        var entry = new PackageEntry
                      {
                        Package = pkg,
                        Spec = spec
                      };

        myStore.Value.AddPackage(entry);
      }
    }

    [CanBeNull]
    private string TeamCityPackagesFile
    {
      get { return WebConfigurationManager.AppSettings["PackagesSpecFile"]; }
    }

    [CanBeNull]
    public string PackageFilesBasePath
    {
      get { return WebConfigurationManager.AppSettings["PackageFilesBasePath"] ?? HostingEnvironment.MapPath("~"); }
    }

    private readonly TeamCityPackagesRepo EMPTY_SPEC =
      new TeamCityPackagesRepo
        {          
          Specs = new TeamCityPackageSpec[0]
        };    
  }
}