using System;
using System.Data.Services;
using System.IO;
using System.Net;
using System.ServiceModel.Activation;
using System.Web.Routing;
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
      var factory = new DataServiceHostFactory();
      var serviceRoute = new ServiceRoute("nuget", factory, typeof (TeamCityPackages))
                           {
                             Defaults = new RouteValueDictionary {{"serviceType", "odata"}},
                             Constraints = new RouteValueDictionary {{"serviceType", "odata"}}
                           };
      
      routes.Add("nuget", serviceRoute);

      routes.MapDelegate("addPackage", "addPackage", c =>
                                                       {
                                                         var data = c.HttpContext.Request.Params;
                                                         
                                                         var buildIdString = data["buildId"];
                                                         var downloadUrl = data["downloadUrl"];
                                                         var packageFile = data["packageFile"];
                                                         var isLatest = data["isLatest"];

                                                         if (downloadUrl == null || packageFile == null || isLatest == null || buildIdString == null)
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

                                                         if (!File.Exists(packageFile))
                                                         {
                                                           WriteStatus(c, HttpStatusCode.InternalServerError, "Failed to find file: " + packageFile);
                                                           return;
                                                         }

                                                         TeamCityContext.Repository.RegisterPackage(new TeamCityPackageSpec
                                                                                                      {
                                                                                                        BuildId = buildId,
                                                                                                        DownloadUrl = downloadUrl, 
                                                                                                        IsLatest = "true".Equals(isLatest, StringComparison.InvariantCultureIgnoreCase),
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