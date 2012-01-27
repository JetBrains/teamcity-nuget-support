using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Serialization;
using JetBrains.Annotations;
using NuGet;
using System.Linq;
using JetBrains.TeamCity.NuGetRunner;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.ListPackages", "Lists packages for given xml list of packages")]
  public class NuGetTeamCityListPackagesCommand : ListCommandBase
  {
    [Option("Path to file containing package-version pairs to check for updates")]
    public string Request { get; set; }

    [Option("Path to file to write result of update check")]
    public string Response { get; set; }

    protected override void ExecuteCommandImpl()
    {
      new AssemblyResolver(GetType().Assembly.GetAssemblyDirectory());
      var reqs = LoadRequests();
      foreach (var p in reqs.Packages)
      {
        //Clean all entries
        p.Entries = null;        
      }

      var sourceToRequest = reqs.Packages.GroupBy(x => x.Feed, Id, StringComparer.InvariantCultureIgnoreCase);
      foreach (var req in sourceToRequest)
      {
        //todo: optimize query to return only required set of versions.

        var packagesToCheck = req.GroupBy(x => x.Id, Id, StringComparer.InvariantCultureIgnoreCase).ToDictionary(x => x.Key, Id<IEnumerable<NuGetPackage>>, StringComparer.InvariantCultureIgnoreCase);
        var ids = packagesToCheck.Select(x => x.Key).Distinct().ToList();

        int count = 0;
        var allPackages = GetAllPackages(req.Key, ids);
        foreach (var feedPackage in allPackages)
        {
          count++;
          IEnumerable<NuGetPackage> enu;
          if (packagesToCheck.TryGetValue(feedPackage.Id, out enu))
          {
            foreach (var p in enu)
            {
              if (!p.VersionChecker(feedPackage)) continue;
              p.AddEntry(new NuGetPackageEntry {Version = feedPackage.VersionString()});
            }
          }
        }

        System.Console.Out.WriteLine("Scanned {0} packages for feed {1}", count, req.Key);
      }

      SaveRequests(reqs);
    }

    private void SaveRequests(NuGetPackages reqs)
    {
      using (var file = File.CreateText(Response))
      {
        GetSerializer().Serialize(file, reqs);
      }
    }

    private NuGetPackages LoadRequests()
    {
      if (!File.Exists(Request))
        throw new CommandLineException("Failed to find file at {0}", Request);

      using (var file = File.OpenRead(Request))
      {
        return (NuGetPackages)GetSerializer().Deserialize(file);
      }
    }

    private static XmlSerializer GetSerializer()
    {
      var parser = new XmlSerializerFactory().CreateSerializer(typeof (NuGetPackages));
      if (parser == null)
        throw new CommandLineException("Failed to create serialized for parameters xml");

      return parser;
    }

    private static T Id<T>(T t)
    {
      return t;
    }
  }

  [Serializable]
  [XmlRoot("nuget-packages")]
  public class NuGetPackages
  {
    [XmlArray("packages")]
    [XmlArrayItem("package")]
    public NuGetPackage[] Packages { get; set; }
  }


  [Serializable]
  [XmlRoot("package")]
  public class NuGetPackage
  {
    [XmlIgnore]
    private readonly Lazy<Func<IPackage, bool>> myVersionSpec;    
    [XmlIgnore]
    private readonly List<NuGetPackageEntry> myEntries = new List<NuGetPackageEntry>();

    public NuGetPackage()
    {
      myVersionSpec = new Lazy<Func<IPackage, bool>>(
        () =>
          {
            try
            {
              var spec = VersionSpec;
              if (string.IsNullOrWhiteSpace(spec)) return True;
              var pSpec = VersionUtility.ParseVersionSpec(spec);
              return xx => new[] {xx}.FindByVersion(pSpec).Any();
            }
            catch(Exception e)
            {
              Console.Out.WriteLine("Error: " + e);
              return True;
            }
          });
    }


    [XmlAttribute("id")]
    public string Id { get; set; }

    [CanBeNull]
    [XmlAttribute("versions")]
    public string VersionSpec { get; set; }

    [CanBeNull]
    [XmlAttribute("source")]
    public string Source { get; set; }

    [XmlIgnore]
    public string Feed
    {
      get { return Source ?? NuGetConstants.DefaultFeedUrl; }
    }

    [XmlArray("package-entries")]
    [XmlArrayItem("package-entry")]
    public NuGetPackageEntry[] Entries
    {
      get
      {
        return myEntries.ToArray();
      }
      set
      {
        myEntries.Clear();
        if (value != null)
        {
          myEntries.AddRange(value);
        }
      }
    }

    public void AddEntry(NuGetPackageEntry entry)
    {      
      myEntries.Add(entry);
    }

    [XmlIgnore]
    public Func<IPackage, bool> VersionChecker
    {
      get { return myVersionSpec.Value; }
    }

    private static bool True<T>(T t)
    {
      return true;
    }
  }

  [Serializable]
  [XmlRoot("package-entry")]
  public class NuGetPackageEntry
  {
    [XmlAttribute("version")]
    public string Version { get; set; }
  }

}