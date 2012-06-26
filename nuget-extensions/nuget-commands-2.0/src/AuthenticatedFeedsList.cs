using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Serializable, XmlRoot("teamcity-feeds-list")]
  public class AuthenticatedFeedsList
  {
    private AuthenticatedFeed[] myCredentials;
    private AuthenticatedFeed[] myFeeds;

    [XmlArray("feeds"), XmlArrayItem("feed")]
    public AuthenticatedFeed[] Feeds
    {
      get { return myFeeds ?? new AuthenticatedFeed[0]; }
      set { myFeeds = value; }
    }

    [XmlArray("credentials"), XmlArrayItem("feed")]
    public AuthenticatedFeed[] Credentials
    {
      get { return myCredentials ?? new AuthenticatedFeed[0]; }
      set { myCredentials = value; }
    }
  }

  [Serializable, XmlRoot("credentials")]
  public class Credentials
  {
    [XmlAttribute("user")]
    public string UserName { get; set; }
    [XmlAttribute("password")]
    public string Password { get; set; }    
  }

  [Serializable, XmlRoot("feed")]
  public class AuthenticatedFeed : Credentials
  {
    private string myUrl;

    [XmlAttribute("url")]
    public string Url
    {
      get { return myUrl == null ? null : myUrl.TrimEnd('/'); }
      set { myUrl = value; }
    }

  }
}
