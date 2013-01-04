using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("nuget-packages")]
  public class NuGetPackages : INuGetPackages
  {
    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public NuGetPackage[] Packages { get; set; }

    [XmlIgnore]
    IEnumerable<INuGetPackage> INuGetPackages.Packages { get { return Packages ?? new INuGetPackage[0]; } }

    public void ClearCheckResults()
    {
      foreach (var p in Packages)
      {
        p.Entries = null;
      }
    }
  }
}