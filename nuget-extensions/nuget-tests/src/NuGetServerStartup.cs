using System.Web.Http;
using NuGet.Server.V2;
using Owin;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public class NuGetServerStartup
  { 
    public void Configuration(IAppBuilder appBuilder)
    {
      var config = new HttpConfiguration();
      appBuilder.UseWebApi(config);

      config.Routes.MapHttpRoute(
          name: "DefaultApi",
          routeTemplate: "api/{controller}/{id}",
          defaults: new { id = RouteParameter.Optional }
      );

      NuGetV2WebApiEnabler.UseNuGetV2WebApiFeed(config,
          routeName: "NuGetPublic",
          routeUrlRoot: "NuGet/public",
          oDatacontrollerName: "NuGetPublicOData");
    }
  }
}
