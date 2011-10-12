using System.Web.Configuration;
using System.Web.Hosting;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RepositoryPaths : IRepositoryPaths
  {
    [CanBeNull]
    public string TeamCityPackagesFile
    {
      get { return WebConfigurationManager.AppSettings["PackagesSpecFile"]; }
    }

    [CanBeNull]
    public string PackageFilesBasePath
    {
      get { return WebConfigurationManager.AppSettings["PackageFilesBasePath"] ?? HostingEnvironment.MapPath("~"); }
    }

  }
}