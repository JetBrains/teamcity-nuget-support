using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  [Serializable]
  [XmlRoot("entry")]
  public class PackageEntry
  {
    [XmlElement("spec")]
    public TeamCityPackageSpec Spec { get; set; }
    [XmlElement("package")]
    public TeamCityPackage Package { get; set; }   
  }
}