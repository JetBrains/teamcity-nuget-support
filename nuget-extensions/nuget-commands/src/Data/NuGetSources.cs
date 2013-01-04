using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("sources")]
  public class NuGetSources : INuGetSources
  {
    [XmlElement("source")]
    public NuGetSource[] Sources { get; set; }

    [XmlIgnore]
    IEnumerable<INuGetSource> INuGetSources.Sources {
      get { return Sources ?? new INuGetSource[0]; }
    }
  }
}
