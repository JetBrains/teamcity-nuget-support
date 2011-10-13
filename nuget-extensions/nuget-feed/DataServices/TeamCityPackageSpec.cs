using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [Serializable, XmlRoot("spec")]
  public class TeamCityPackageSpec
  {
    [XmlAttribute("buildType")]
    public string BuildType { get; set; }

    [XmlAttribute("buildId")]
    public long BuildId { get; set; }
    
    [XmlAttribute("downloadUrl")]
    public string DownloadUrl { get; set; }

    [XmlAttribute("packageFile")]
    public string PackageFile { get; set; }
  }
}