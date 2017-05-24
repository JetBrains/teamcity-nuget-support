using System.Collections.Generic;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public static class FeedConfigurationConstants
  {
    public const string TEAMCITY_URL = "TeamCityBaseUri";
    public const string TOKEN = "Token";


    public static IEnumerable<string> AllKeys
    {
      get
      {
        yield return TEAMCITY_URL;
        yield return TOKEN;
      }
    }
  }
}