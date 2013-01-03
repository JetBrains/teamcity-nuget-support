using System;
using System.Collections.Generic;
using System.Linq;
using System.Xml.Serialization;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("package")]
  public class NuGetPackage : NuGetSource
  {
    [XmlIgnore] private readonly Lazy<Func<IPackage, bool>> myVersionSpec;
    [XmlIgnore] private readonly List<NuGetPackageEntry> myEntries = new List<NuGetPackageEntry>();

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
            catch (Exception e)
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

    [XmlIgnore]
    public NuGetSource Feed
    {
      get { return Source != null ?  this : FromFeedUrl(NuGetConstants.DefaultFeedUrl); }
    }

    [XmlArray("package-entries")]
    [XmlArrayItem("package-entry")]
    public NuGetPackageEntry[] Entries
    {
      get { return myEntries.ToArray(); }
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
}