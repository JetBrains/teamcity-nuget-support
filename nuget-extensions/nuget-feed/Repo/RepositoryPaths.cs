using System.Web.Configuration;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class RepositoryPaths : IRepositoryPaths
  {
    public string FetchPacakgesUri
    {
      get { return WebConfigurationManager.AppSettings["PackagesSpecUri"]; }
    }
  }
}