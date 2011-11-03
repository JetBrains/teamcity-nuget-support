using System;
using System.Collections.Generic;
using System.Xml.Serialization;
using System.Linq;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  [Serializable]
  [XmlRoot("package-list")]
  public class MemoryRepo : ITeamCityPackagesRepo
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    [XmlIgnore]
    private readonly List<TeamCityPackage> mySpecs = new List<TeamCityPackage>();

    private readonly Dictionary<string, List<TeamCityPackage>> myIdIndex
      = new Dictionary<string, List<TeamCityPackage>>(StringComparer.InvariantCultureIgnoreCase);

    private readonly Dictionary<string, TeamCityPackage> myLatestVersionsIndex
      = new Dictionary<string, TeamCityPackage>(StringComparer.InvariantCultureIgnoreCase);
      
    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public TeamCityPackage[] Specs
    {
      get { return mySpecs.ToArray(); } 
      set { 
        mySpecs.Clear();
        if (value != null)
        {
          mySpecs.AddRange(value);
          foreach (var entry in value)
          {
            AddSpec(entry);
          }
        }
      }
    }

    public void AddSpecs(IEnumerable<TeamCityPackage> packages)
    {
      foreach (var package in packages)
      {
        AddSpec(package);
      }
      
    }

    public void AddSpec(TeamCityPackage entry)
    {
      mySpecs.Add(entry);

      var id = entry.Id;
      List<TeamCityPackage> list;
      if (!myIdIndex.TryGetValue(id, out list))
      {
        list = new List<TeamCityPackage>();
        myIdIndex[id] = list;
      }
      list.Add(entry);

      //Assume latest is the oldest to avoid comparing buildIds
      myLatestVersionsIndex[id] = entry;
    }

    public IEnumerable<TeamCityPackage> GetAllPackages()
    {
      return Specs;
    }

    public IEnumerable<TeamCityPackage> FilterById(IEnumerable<string> ids)
    {
      return ids
        .Where(myIdIndex.ContainsKey)
        .SelectMany(x => myIdIndex[x]);
    }

    public IEnumerable<TeamCityPackage> FiltetByIdLatest(IEnumerable<string> ids)
    {
      return ids
        .Where(myLatestVersionsIndex.ContainsKey)
        .Select(x => myLatestVersionsIndex[x]);
    }
  }
}