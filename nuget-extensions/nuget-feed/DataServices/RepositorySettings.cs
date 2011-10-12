using System;
using System.Web.Configuration;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class RepositorySettings : IRepositorySettings
  {
    private const string PACKAGE_FILE_KEY = "PackagesSpecFile";
    private const string PACKAGE_BASE_PATH_KEY = "PackageFilesBasePath";

    private readonly string myPackagesFile;
    private readonly string myPackagesBase;

    public RepositorySettings()
    {
      myPackagesFile = WebConfigurationManager.AppSettings[PACKAGE_FILE_KEY];
      myPackagesBase = WebConfigurationManager.AppSettings[PACKAGE_BASE_PATH_KEY];

      if (string.IsNullOrWhiteSpace(myPackagesFile) || string.IsNullOrWhiteSpace(myPackagesBase))
        throw new Exception(string.Format("Settings are missing. Check you specify {0} and {1}", PACKAGE_FILE_KEY,
                                          PACKAGE_BASE_PATH_KEY));        
    }

    public string PackagesFile
    {
      get { return myPackagesFile; }
    }

    public string PackagesBase
    {
      get { return myPackagesBase; }
    }
    
  }
}