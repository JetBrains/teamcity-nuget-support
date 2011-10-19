using System;
using System.Collections.Generic;
using System.Xml.Serialization;
using System.Linq;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [Serializable]
  [XmlRoot("package-list")]
  public class TeamCityPackagesRepo
  {
    private readonly object myLock = new object();

    [XmlIgnore]
    private readonly List<TeamCityPackageEntry> mySpecs = new List<TeamCityPackageEntry>();

    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public TeamCityPackageEntry[] Specs
    {
      get
      {
        lock (myLock)
        {
          return mySpecs.ToArray();
        }
      } 
      set {
        lock (myLock)
        {
          mySpecs.Clear();
          if (value != null) mySpecs.AddRange(value);
        }
      }
    }

    public void AddSpec([NotNull] TeamCityPackageEntry spec)
    {
      if (spec == null) throw new ArgumentNullException("spec");
      if (spec.Package == null) throw new ArgumentException("Spec must contain package to be added");

      lock (myLock)
      {        
        var packageId = spec.Package.Id;

        foreach (var pkg in Specs
          .Where(x => x.Package.Id.ToLower() == packageId)
          .Select(x => x.Package)
          .Where(x => x != null))
          pkg.IsLatestVersion = false;

        mySpecs.Add(spec);
        spec.Package.IsLatestVersion = true;
      }
    }

    public void RemoveSpecs(IEnumerable<TeamCityPackageEntry> entries)
    {
      lock(myLock)
      {
        foreach (var e in entries)
        {
          mySpecs.Remove(e);
        }
      }
    }
  }
}