using System;
using System.Collections.Generic;
using System.Text;
using System.Xml.Serialization;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
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
