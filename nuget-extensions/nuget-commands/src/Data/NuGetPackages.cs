using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("nuget-packages")]
  public class NuGetPackages
  {
    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public NuGetPackage[] Packages { get; set; }

    public void ClearCheckResults()
    {
      foreach (var p in Packages)
      {
        p.Entries = null;
      }
    }
  }
}