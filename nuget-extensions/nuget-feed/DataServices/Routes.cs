using System;
using System.Data.Services;
using System.IO;
using System.Net;
using System.ServiceModel.Activation;
using System.Web.Routing;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using NuGetRoutes = JetBrains.TeamCity.NuGet.Feed.DataServices.NuGetRoutes;
using RouteMagic;

[assembly: WebActivator.PreApplicationStartMethod(typeof (NuGetRoutes), "Start")]

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class NuGetRoutes
  {
    public static void Start()
    {
      MapRoutes(RouteTable.Routes);
    }

    private static void MapRoutes(RouteCollection routes)
    {
      // The default route is http://{root}/nuget/Packages

      routes.Add("nuget",
                 new ServiceRoute("nuget", new DataServiceHostFactory(), typeof (TeamCityPackages))
                   {
                     Defaults = new RouteValueDictionary {{"serviceType", "odata"}},
                     Constraints = new RouteValueDictionary {{"serviceType", "odata"}}
                   });

      routes.MapDelegate("addPackage", "addPackage", c =>
                                                       {
                                                         var data = c.HttpContext.Request.Params;
                                                         
                                                         var buildType = data["buildType"];
                                                         var buildIdString = data["buildId"];
                                                         var downloadUrl = data["downloadUrl"];
                                                         var packageFile = data["packageFile"];

                                                         if (downloadUrl == null || packageFile == null || buildIdString == null || buildType == null)
                                                         {
                                                           WriteStatus(c, HttpStatusCode.InternalServerError, "Missing parameters");
                                                           return;
                                                         }

                                                         long buildId;
                                                         if (!long.TryParse(buildIdString, out buildId))
                                                         {
                                                           WriteStatus(c, HttpStatusCode.InternalServerError, "Failed to parse buildId");
                                                           return;
                                                         }

                                                         var repo = TeamCityContext.Repository;
                                                         var basePath = repo.PackageFilesBasePath;
                                                         if (basePath == null || !File.Exists(Path.Combine(basePath, packageFile)))
                                                         {
                                                           WriteStatus(c, HttpStatusCode.InternalServerError, "Failed to find file: " + packageFile);
                                                           return;
                                                         }

                                                         repo.RegisterPackage(new TeamCityPackageSpec
                                                                                {
                                                                                  BuildType = buildType,
                                                                                  BuildId = buildId,
                                                                                  DownloadUrl = downloadUrl,
                                                                                  PackageFile = packageFile,
                                                                                });

                                                         WriteStatus(c, HttpStatusCode.OK, "Package added");
                                                       });
    }

    private static void WriteStatus(RequestContext context, HttpStatusCode statusCode, string body = null)
    {
      context.HttpContext.Response.StatusCode = (int)statusCode;
      if (!String.IsNullOrEmpty(body))
      {
        context.HttpContext.Response.Write(body);
      }
    }

  }
}