using System;
using System.Net;
using System.Web.Routing;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class CleanOldPackagesHandler : HandlerBase 
  {
    private readonly LightPackageRepository myRepo;

    public CleanOldPackagesHandler(RequestContext context, LightPackageRepository repo) : base(context)
    {
      myRepo = repo;
    }

    public void OnClean()
    {
      try {
        myRepo.CleanupObsoletePackages();
        WriteStatus(HttpStatusCode.OK, "Obsolete packages cleaned");
      } catch(Exception e)
      {
        WriteStatus(HttpStatusCode.InternalServerError, string.Format("Unexpected error: {0}\r\n{1}", e.Message, e));
      }
    }
  }
}