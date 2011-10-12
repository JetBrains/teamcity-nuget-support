using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  [Serializable, XmlRoot("package-enrty")]
  public class TeamCityPackageEntry
  {
    [XmlAttribute("version")]
    public int Version { get; set; }

    [XmlElement("spec")]
    public TeamCityPackageSpec Spec { get; set; }

    [XmlElement("package")]
    public TeamCityPackage Package { get; set; }
  }
}