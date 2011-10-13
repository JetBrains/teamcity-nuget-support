using System.Data.Services;
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

      routes.Add("nuget",
                 new ServiceRoute("nuget", new DataServiceHostFactory(), typeof (TeamCityPackages))
                   {
                     Defaults = new RouteValueDictionary {{"serviceType", "odata"}},
                     Constraints = new RouteValueDictionary {{"serviceType", "odata"}}
                   });
    }



  }
}