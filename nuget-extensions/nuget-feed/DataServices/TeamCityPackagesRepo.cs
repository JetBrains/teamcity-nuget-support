using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [Serializable]
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

    [XmlAttribute("isLatest")]
    public bool IsLatest { get; set; }
  }

  [Serializable]
  [XmlRoot("package-list")]
  public class TeamCityPackagesRepo
  {
    [XmlIgnore]
    private readonly List<TeamCityPackageSpec> mySpecs = new List<TeamCityPackageSpec>();

    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public TeamCityPackageSpec[] Specs
    {
      get { return mySpecs.ToArray(); } 
      set { 
        mySpecs.Clear();
        if (value != null)
          mySpecs.AddRange(value);
      }
    }

    public void AddSpec(TeamCityPackageSpec spec)
    {
      mySpecs.Add(spec);      
    }
  }
}