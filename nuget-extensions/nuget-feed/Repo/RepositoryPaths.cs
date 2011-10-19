using System.Web.Configuration;
using System.Web.Hosting;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RepositoryPaths : IRepositoryPaths
  {
    public string TeamCityPackagesFile
    {
      get { return WebConfigurationManager.AppSettings["PackagesSpecFile"]; }
    }

    public string PackageFilesBasePath
    {
      get { return WebConfigurationManager.AppSettings["PackageFilesBasePath"] ?? HostingEnvironment.MapPath("~"); }
    }

  }
}