using System;
using System.IO;
using System.Net;
using System.Web.Routing;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class AddPackageHandler
  {
    private readonly RequestContext myContext;
    private readonly IRepositorySettings mySettings;
    private readonly LightPackageRepository myRepo;

    public AddPackageHandler(RequestContext context, IRepositorySettings settings, LightPackageRepository repo)
    {
      myContext = context;
      mySettings = settings;
      myRepo = repo;
    }

    public void OnAddPackage()
    {
      var data = myContext.HttpContext.Request.Params;

      var buildType = data["buildType"];
      var buildIdString = data["buildId"];
      var downloadUrl = data["downloadUrl"];
      var packageFile = data["packageFile"];

      if (downloadUrl == null || packageFile == null || buildIdString == null || buildType == null)
      {
        WriteStatus(HttpStatusCode.InternalServerError, "Missing parameters");
        return;
      }

      long buildId;
      if (!long.TryParse(buildIdString, out buildId))
      {
        WriteStatus(HttpStatusCode.InternalServerError, "Failed to parse buildId");
        return;
      }

      try
      {
        if (!File.Exists(Path.Combine(mySettings.PackagesBase, packageFile)))
        {
          WriteStatus(HttpStatusCode.InternalServerError, "Failed to find file: " + packageFile);
          return;
        }

        var entry = TeamCityZipPackageFactory.LoadPackage(mySettings, new TeamCityPackageSpec
                                                                        {
                                                                          BuildType = buildType,
                                                                          BuildId = buildId,
                                                                          DownloadUrl = downloadUrl,
                                                                          PackageFile = packageFile,
                                                                        });
        if (entry == null)
        {
          WriteStatus(HttpStatusCode.InternalServerError, "Failed to read package file: " + packageFile);
          return;
        }

        myRepo.RegisterPackage(entry);
        WriteStatus(HttpStatusCode.OK, "Package added");
      } catch(Exception e)
      {
        WriteStatus(HttpStatusCode.InternalServerError, string.Format("Unexpected failure. {0}\r\n{1}", e.Message, e));
      }
    }

    private void WriteStatus(HttpStatusCode statusCode, string body)
    {
      myContext.HttpContext.Response.StatusCode = (int)statusCode;
      if (!String.IsNullOrEmpty(body))
        myContext.HttpContext.Response.Write(body);
    }
  }
}