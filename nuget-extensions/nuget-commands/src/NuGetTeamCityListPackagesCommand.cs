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
      foreach (var sourceRequest in sourceToRequest)
      {
        ProcessPackageSource(sourceRequest.Key, sourceRequest.ToList());
      }

      SaveRequests(reqs);
    }

    private void ProcessPackageSource(string source, List<NuGetPackage> request)
    {
      //todo: optimize query to return only required set of versions.
      foreach (var req in new[]
                              {
                                new { Data = request.Where(x => x.VersionSpec == null && x.IncludePrerelease).ToArray(), FetchOption = PackageFetchOption.IncludeLatestAndPrerelease }, 
                                new { Data = request.Where(x => x.VersionSpec == null && !x.IncludePrerelease).ToArray(), FetchOption = PackageFetchOption.IncludeLatest }, 
                                new { Data = request.Where(x => x.VersionSpec != null).ToArray(), FetchOption = PackageFetchOption.IncludeAll }
                              })
      {
        ProcessPackages(source, req.FetchOption, req.Data);
      }
    }

    private void ProcessPackages(string source, PackageFetchOption fetchOptions, IEnumerable<NuGetPackage> package)
    {
      var packageToData = package
        .GroupBy(x => x.Id, Id, PACKAGE_ID_COMPARER)
        .ToDictionary(x=>x.Key, Id, PACKAGE_ID_COMPARER);

      if (packageToData.Count == 0) return;
      var data = GetAllPackages(source, fetchOptions, packageToData.Keys);
      var count = 0;
      if (data != null)
      {
      foreach (var p in data)
      {
        count++;
        IGrouping<string, NuGetPackage> res;
        if (!packageToData.TryGetValue(p.Id, out res)) continue;

        foreach (var r in res)
        {
          if (!r.VersionChecker(p)) continue;
          r.AddEntry(new NuGetPackageEntry {Version = p.VersionString()});
        }
      }
      }

      System.Console.Out.WriteLine("Scanned {0} packages for feed {1}", count, source);
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

    private static readonly IEqualityComparer<string> PACKAGE_ID_COMPARER = StringComparer.InvariantCultureIgnoreCase;
  }

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
              return xx => Enumerable.Any(new[] {xx}.FindByVersion(pSpec));
            }
            catch(Exception e)
            {
              Console.Out.WriteLine("Error: " + e);
              return True;
            }
          });
    }

    [XmlAttribute("include-prerelease")]
    public string IncludePrereleaseInternal { get; set; }

    [XmlIgnore]
    public bool IncludePrerelease
    {
      get { return "True".Equals(IncludePrereleaseInternal ?? "", StringComparison.InvariantCultureIgnoreCase); }
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