using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [Serializable, XmlRoot("package-entry")]
  public class TeamCityPackageEntry
  {
    [XmlElement("spec")]
    public TeamCityPackageSpec Spec { get; set; }

    [XmlElement("package")]
    public TeamCityPackage Package { get; set; }
  }
}