using System;
using System.IO;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class LightPackageRepository
  {
    private readonly TeamCityPackagesRepo EMPTY_SPEC = new TeamCityPackagesRepo();

    private static readonly object CONFIG_ACCESS_LOG = new object();
    private readonly IRepositorySettings mySettings;

    private readonly Lazy<TeamCityPackagesRepo> myRepo;

    public LightPackageRepository(IRepositorySettings settings)
    {
      mySettings = settings;
      myRepo = new Lazy<TeamCityPackagesRepo>(LoadPackageSpec, true);      
    }

    public IQueryable<TeamCityPackage> GetPackages()
    {
      return
        from spec in Repo.Specs.AsQueryable()
        let x = spec.Package
        where x != null
        select x;
    }

    private TeamCityPackagesRepo Repo
    {
      get { return myRepo.Value; }
    }

    private TeamCityPackagesRepo LoadPackageSpec()
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var xmlFile = mySettings.PackagesFile;
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
        var xmlFile = mySettings.PackagesFile;

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

    public void RegisterPackage(TeamCityPackageEntry spec)
    {
      lock (CONFIG_ACCESS_LOG)
      {
        var repo = Repo;
        repo.AddSpec(spec);
        StorePackagesSpec(repo);
      }
    }
  }
}