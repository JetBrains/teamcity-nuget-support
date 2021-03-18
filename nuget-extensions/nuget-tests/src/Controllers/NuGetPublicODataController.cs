using NuGet.Server.V2.Controllers;

namespace JetBrains.TeamCity.NuGet.Tests.Controllers
{
  public class NuGetPublicODataController : NuGetODataController
  {
    public NuGetPublicODataController()
      : base(ServerPackageRepository.Instance)
    {
    }
  }
}
