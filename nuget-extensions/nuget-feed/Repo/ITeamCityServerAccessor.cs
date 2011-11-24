using System;
using System.IO;
using System.Net;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public interface ITeamCityServerAccessor
  {
    T ProcessRequest<T>(string urlSuffix, Func<HttpWebResponse, TextReader, T> result);

    [NotNull]
    string TeamCityUrl { get; }

    [NotNull]
    ITeamCityServerAccessor ForUser([CanBeNull] string userId);
  }
}