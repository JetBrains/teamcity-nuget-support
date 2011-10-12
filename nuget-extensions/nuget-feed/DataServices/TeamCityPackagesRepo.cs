using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
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