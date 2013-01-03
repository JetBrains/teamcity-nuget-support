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
}
