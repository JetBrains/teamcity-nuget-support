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
    [XmlAttribute("url")]
    public string Url { get; set; }
    [XmlAttribute("user")]
    public string UserName { get; set; }
    [XmlAttribute("password")]
    public string Password { get; set; }
  }
}
