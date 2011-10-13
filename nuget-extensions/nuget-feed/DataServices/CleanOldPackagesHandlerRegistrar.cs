using System.Web.Routing;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.DataServices;
using RouteMagic;

[assembly: WebActivator.PreApplicationStartMethod(typeof(CleanOldPackagesHandlerRegistrar), "Start")]

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class CleanOldPackagesHandlerRegistrar
  {
    [UsedImplicitly]
    public static void Start()
    {
      RouteTable.Routes.MapDelegate("cleanup", "cleanup",
                                    c => new CleanOldPackagesHandler(c, TeamCityContext.Repository).OnClean());
    }
  }
}