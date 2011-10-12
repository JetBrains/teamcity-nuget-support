using System.IO;
using System.Linq;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class LightPackageRepository
  {
    private readonly TeamCityPackagesRepo EMPTY_SPEC = new TeamCityPackagesRepo();

    private static readonly object CONFIG_ACCESS_LOG = new object();
    private readonly IRepositorySettings mySettings;

    public LightPackageRepository(IRepositorySettings settings)
    {
      mySettings = settings;
    }

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
        if (!File.Exists(xmlFile)) return EMPTY_SPEC;

        try
        {
          using (var tw = File.OpenRead(xmlFile))
          {
            return (TeamCityPackagesRepo) XmlSerializers<TeamCityPackagesRepo>.Create().Deserialize(tw);
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

        var parent = Path.GetDirectoryName(xmlFile);
        if (parent == null) return;

        if (!Directory.Exists(parent))
          Directory.CreateDirectory(parent);

        using (var file = File.OpenWrite(xmlFile))
        {
          XmlSerializers<TeamCityPackagesRepo>.Create().Serialize(file, spec);
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

    [NotNull]
    private string TeamCityPackagesFile
    {
      get { return mySettings.PackagesFile; }
    }

    [NotNull]
    public string PackageFilesBasePath
    {
      get { return mySettings.PackagesBase; }
    }    
  }
}