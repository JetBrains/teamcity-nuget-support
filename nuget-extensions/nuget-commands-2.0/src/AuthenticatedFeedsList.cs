using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Serializable, XmlRoot("teamcity-feeds-list")]
  public class AuthenticatedFeedsList
  {
    [XmlArray("feeds"), XmlArrayItem("feed")]
    public AuthenticatedFeed[] Feeds { get; set; }
  }

  [Serializable, XmlRoot("feed")]
  public class AuthenticatedFeed
  {
    private string myUrl;

    [XmlAttribute("url")]
    public string Url
    {
      get { return myUrl == null ? null : myUrl.TrimEnd('/'); }
      set { myUrl = value; }
    }

    [XmlAttribute("user")]
    public string UserName { get; set; }
    [XmlAttribute("password")]
    public string Password { get; set; }
  }
}
