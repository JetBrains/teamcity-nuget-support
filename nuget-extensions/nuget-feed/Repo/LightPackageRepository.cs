using System;
using System.IO;
using System.Linq;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class LightPackageRepository
  {
    private readonly IRepositoryPaths myPaths;
    private static readonly XmlSerializerFactory myXmlSerializerFactory = new XmlSerializerFactory();
    private static readonly object CONFIG_ACCESS_LOG = new object();

    private readonly Lazy<ITeamCityPackagesRepo> myLazyRepo;

    public LightPackageRepository(IRepositoryPaths paths)
    {
      myPaths = paths;
      myLazyRepo = new Lazy<ITeamCityPackagesRepo>(LoadDatabase, true);
    }

    public string PackageFilesBasePath
    {
      get { return myPaths.PackageFilesBasePath; }
    }

    public IQueryable<TeamCityPackage> GetPackages()
    {
      return new TeamCityQueryablePackages(FetchPackageSpec()).Query;
    }

    private ITeamCityPackagesRepo FetchPackageSpec()
    {
      return myLazyRepo.Value;
    }

    private TeamCityPackagesRepo LoadDatabase()
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var xmlFile = myPaths.TeamCityPackagesFile;
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

    private void StorePackagesSpec(ITeamCityPackagesRepo spec)
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var xmlFile = myPaths.TeamCityPackagesFile;
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
        var path = myPaths.PackageFilesBasePath;
        if (path == null) return;

        var pkg = TeamCityZipPackageFactory.LoadPackage(Path.Combine(path, spec.PackageFile), spec);
        var entry = new TeamCityPackageEntry
                      {
                        Package = pkg,
                        Spec = spec
                      };

        repo.AddSpec(entry);
        StorePackagesSpec(repo);
      }
    }

    private readonly TeamCityPackagesRepo EMPTY_SPEC = new TeamCityPackagesRepo();
  }
}