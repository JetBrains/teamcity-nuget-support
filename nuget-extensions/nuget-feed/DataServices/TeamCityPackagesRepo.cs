using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [Serializable]
  public class TeamCityPackageSpec
  {
    [XmlAttribute("buildId")]
    public long BuildId { get; set; }
    [XmlAttribute("downloadUrl")]
    public string DownloadUrl { get; set; }
    [XmlAttribute("packageFile")]
    public string PackageFile { get; set; }
  }

  [Serializable]
  [XmlRoot("package-list")]
  public class TeamCityPackagesRepo
  {
    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public TeamCityPackageSpec[] Specs { get; set; }
  }
}