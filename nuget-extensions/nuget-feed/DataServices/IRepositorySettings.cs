using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public interface IRepositorySettings
  {
    [NotNull]
    string PackagesFile { get; }
    
    [NotNull]
    string PackagesBase { get; }
  }
}