using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web.Configuration;
using System.Web.Hosting;
using System.Xml.Serialization;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class LightPackageRepository
  {
    private readonly IEnumerable<TeamCityPackageSpec> EMPTY_SPEC = new TeamCityPackageSpec[0];
    private readonly XmlSerializerFactory myXmlSerializerFactory = new XmlSerializerFactory();


    public IQueryable<TeamCityPackage> GetPackages()
    {
      return
        from spec in FetchPackageSpec().AsQueryable()
        let path = spec.PackageFile
        select new TeamCityZipPackage(path).ToPackage;
    }

    private IEnumerable<TeamCityPackageSpec> FetchPackageSpec()
    {
      var xmlFile = TeamCityPackagesFile;
      if (xmlFile == null) return EMPTY_SPEC;

      var ser = myXmlSerializerFactory.CreateSerializer(typeof(TeamCityPackagesRepo));
      if (ser == null) return EMPTY_SPEC;
      try
      {
        using (var tw = File.OpenRead(xmlFile))
        {
          var info = (TeamCityPackagesRepo)ser.Deserialize(tw);
          return info.Specs;
        }
      }
      catch
      {
        //TODO: catch exception
        return EMPTY_SPEC;
      }
    }

    [CanBeNull]
    private string TeamCityPackagesFile
    {
      get
      {
        var xmlFile = WebConfigurationManager.AppSettings["PackagesSpecFile"];
        if (xmlFile == null) return null;

        var fsFile = HostingEnvironment.MapPath(xmlFile);
        if (fsFile == null) return null;

        if (!File.Exists(fsFile)) return null;
        return fsFile;
      }
    }
  }
}