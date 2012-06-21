using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Serializable]
  [XmlRoot("package-entry")]
  public class NuGetPackageEntry
  {
    [XmlAttribute("version")]
    public string Version { get; set; }
  }
}
