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
    private readonly List<TeamCityPackageEntry> mySpecs = new List<TeamCityPackageEntry>();

    private readonly Dictionary<string, List<TeamCityPackageEntry>> myIdIndex 
      = new Dictionary<string, List<TeamCityPackageEntry>>(StringComparer.InvariantCultureIgnoreCase);

    private readonly Dictionary<string, TeamCityPackageEntry> myLatestVersionsIndex 
      = new Dictionary<string, TeamCityPackageEntry>(StringComparer.InvariantCultureIgnoreCase);
      
    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public TeamCityPackageEntry[] Specs
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

    public void AddSpec(TeamCityPackageEntry entry)
    {
      mySpecs.Add(entry);

      var id = entry.Package.Id;
      List<TeamCityPackageEntry> list;
      if (!myIdIndex.TryGetValue(id, out list))
      {
        list = new List<TeamCityPackageEntry>();
        myIdIndex[id] = list;
      }
      list.Add(entry);

      //Assume latest is the oldest to avoid comparing buildIds
      myLatestVersionsIndex[id] = entry;
    }

    public IEnumerable<TeamCityPackage> GetAllPackages()
    {
      return Specs.Select(x=>x.Package);
    }

    public IEnumerable<TeamCityPackage> FilterById(IEnumerable<string> ids)
    {
      return ids
        .Where(myIdIndex.ContainsKey)
        .SelectMany(x => myIdIndex[x])
        .Select(x => x.Package);
    }

    public IEnumerable<TeamCityPackage> FiltetByIdLatest(IEnumerable<string> ids)
    {
      return ids
        .Where(myLatestVersionsIndex.ContainsKey)
        .Select(x => myLatestVersionsIndex[x])
        .Select(x => x.Package);
    }
  }
}