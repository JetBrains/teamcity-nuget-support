using System.IO;
using System.Linq;
using System.Web.Configuration;
using System.Web.Hosting;
using System.Xml.Serialization;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class LightPackageRepository
  {
    private readonly XmlSerializerFactory myXmlSerializerFactory = new XmlSerializerFactory();
    private static readonly object CONFIG_ACCESS_LOG = new object();

    public IQueryable<TeamCityPackage> GetPackages()
    {
      var basePath = PackageFilesBasePath;      
      var repo = FetchPackageSpec();      
      return
        from spec in repo.Specs.AsQueryable()
        let path = Path.Combine(basePath, spec.PackageFile)
        where File.Exists(path)
        select TeamCityZipPackageFactory.LoadPackage(path, spec);
    }

    private TeamCityPackagesRepo FetchPackageSpec()
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var xmlFile = TeamCityPackagesFile;
        if (xmlFile == null || !File.Exists(xmlFile)) return EMPTY_SPEC;

        var ser = myXmlSerializerFactory.CreateSerializer(typeof (TeamCityPackagesRepo));
        if (ser == null) return EMPTY_SPEC;
        try
        {
          using (var tw = File.OpenRead(xmlFile))
          {
            var info = (TeamCityPackagesRepo) ser.Deserialize(tw);
            return info;
          }
        }
        catch
        {
          //TODO: catch exception
          return EMPTY_SPEC;
        }
      }
    }

    private void StorePackagesSpec(TeamCityPackagesRepo spec)
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var xmlFile = TeamCityPackagesFile;
        if (xmlFile == null) return;

        var parent = Path.GetDirectoryName(xmlFile);
        if (parent == null) return;

        if (!Directory.Exists(parent))
          Directory.CreateDirectory(parent);

        var ser = myXmlSerializerFactory.CreateSerializer(typeof (TeamCityPackagesRepo));
        if (ser == null) return;
        using (var file = File.OpenWrite(xmlFile))
        {
          ser.Serialize(file, spec);
        }
      }
    }

    public void RegisterPackage(TeamCityPackageSpec spec)
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var repo = FetchPackageSpec();
        repo.AddSpec(spec);
        StorePackagesSpec(repo);
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