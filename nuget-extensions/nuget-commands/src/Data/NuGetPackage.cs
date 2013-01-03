using System;
using System.Collections.Generic;
using System.Linq;
using System.Xml.Serialization;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("source")]
  public class NuGetSource
  {
    [NotNull]
    [XmlAttribute("source")]    
    public String Source { get; set; }

    [CanBeNull]
    [XmlAttribute("username")]    
    public String Usename { get; set; }
    
    [XmlIgnore]
    [CanBeNull]
    public String Password { get; private set; }

    [CanBeNull]
    [XmlAttribute("password")]
    public String SecuredPassword
    {
      get { return Password; }
      set { Password = value; }
    }

    [NotNull]
    public static NuGetSource FromFeedUrl([NotNull] string url)
    {
      return new NuGetSource { Source = url };
    }

    public static readonly IEqualityComparer<NuGetSource> Comparer = new ComparerImpl();

    private class ComparerImpl : IEqualityComparer<NuGetSource>
    {
      private readonly IEqualityComparer<String> SourceComparer = StringComparer.InvariantCultureIgnoreCase; 

      public bool Equals(NuGetSource x, NuGetSource y)
      {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;

        if (!SourceComparer.Equals(x.Source, y.Source)) return false;
        if (x.Usename != y.Usename) return false;
        if (x.Password != y.Password) return false;
        return true;
      }

      public int GetHashCode(NuGetSource obj)
      {
        if (obj == null) return 0;
        return SourceComparer.GetHashCode(obj.Source);
      }
    }
  }


  [Serializable]
  [XmlRoot("package")]
  public class NuGetPackage
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

    [CanBeNull]
    [XmlElement("source")]
    public NuGetSource Source { get; set; }

    [XmlIgnore]
    public NuGetSource Feed
    {
      get { return Source ?? NuGetSource.FromFeedUrl(NuGetConstants.DefaultFeedUrl); }
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