using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public interface IRepositoryPaths
  {
    [NotNull]
    string FetchPacakgesUri { get; }
  }
}