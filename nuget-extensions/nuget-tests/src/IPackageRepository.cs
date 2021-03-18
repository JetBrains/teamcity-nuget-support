
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using NuGet.Server.Core.Infrastructure;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public interface IPackageRepository
  {
    IEnumerable<IServerPackage> GetPackages(ClientCompatibility compatibility, CancellationToken token);
    IEnumerable<IServerPackage> SearchAsync(string searchTerm, IEnumerable<string> targetFrameworks,
      bool allowPrereleaseVersions, bool allowUnlistedVersions, ClientCompatibility compatibility,
      CancellationToken token);
  }
}
