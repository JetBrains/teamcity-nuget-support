using System.Web.Routing;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.DataServices;
using RouteMagic;

[assembly: WebActivator.PreApplicationStartMethod(typeof(AddPackageHandlerRegistrar), "Start")]

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class AddPackageHandlerRegistrar
  {
    [UsedImplicitly]
    public static void Start()
    {
      RouteTable.Routes.MapDelegate("addPackage", "addPackage", c => new AddPackageHandler(c, TeamCityContext.Settings, TeamCityContext.Repository).OnAddPackage());
    }
  }
}