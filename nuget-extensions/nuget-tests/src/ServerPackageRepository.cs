using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using NuGet;
using NuGet.Server.Core.Infrastructure;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public class ServerPackageRepository : IServerPackageRepository
  {
    private static readonly ServerPackageRepository myInstance;

    public static ServerPackageRepository Instance
    {
      get { return myInstance; }
    }

    static ServerPackageRepository()
    {
      myInstance = new ServerPackageRepository();
    }

    private IPackageRepository myServerPackageRepositoryImplementation;

    private ServerPackageRepository()
    { 
    }

    public IDisposable SetupRepository(IPackageRepository repository)
    {
      if (myServerPackageRepositoryImplementation != null) throw new InvalidOperationException("Repository has already been initialized");

      myServerPackageRepositoryImplementation = repository;

      return new DisposableAction(() => myServerPackageRepositoryImplementation = null);
    }

    public Task AddPackageAsync(IPackage package, CancellationToken token)
    {
      throw new NotImplementedException();
    }
    public Task<IEnumerable<IServerPackage>> GetPackagesAsync(ClientCompatibility compatibility, CancellationToken token)
    {
      return Task.FromResult(myServerPackageRepositoryImplementation.GetPackages(compatibility, token));
    }

    public Task<IEnumerable<IServerPackage>> SearchAsync(string searchTerm, IEnumerable<string> targetFrameworks, bool allowPrereleaseVersions,
      ClientCompatibility compatibility, CancellationToken token)
    {
      return Task.FromResult(myServerPackageRepositoryImplementation.SearchAsync(searchTerm, targetFrameworks, allowPrereleaseVersions, false, compatibility, token));
    }

    public Task<IEnumerable<IServerPackage>> SearchAsync(string searchTerm, IEnumerable<string> targetFrameworks, bool allowPrereleaseVersions,
      bool allowUnlistedVersions, ClientCompatibility compatibility, CancellationToken token)
    {
      return Task.FromResult(myServerPackageRepositoryImplementation.SearchAsync(searchTerm, targetFrameworks, allowPrereleaseVersions, allowUnlistedVersions, compatibility, token));
    }

    public Task ClearCacheAsync(CancellationToken token)
    {
      throw new NotImplementedException();
    }

    public Task RemovePackageAsync(string packageId, SemanticVersion version, CancellationToken token)
    {
      throw new NotImplementedException();
    }

    public string Source => string.Empty;
  }
}
