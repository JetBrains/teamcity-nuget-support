using System;
using System.Collections.Generic;
using System.Text;
using System.Xml.Serialization;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("source")]
  public class NuGetSource : INuGetSource
  {
    [XmlAttribute("source")]
    public String Source { get; set; }

    [XmlAttribute("username")]
    public String Username { get; set; }

    [XmlIgnore]
    public String Password { get; set; }

    [XmlAttribute("password")]
    public String SecuredPassword
    {
      get { return Password; }
      set { Password = value; }
    }

    [NotNull]
    public static NuGetSource FromFeedUrl([NotNull] string url)
    {
      return new NuGetSource {Source = url};
    }

    [XmlIgnore]
    public bool HasCredentials
    {
      get { return !String.IsNullOrWhiteSpace(Username); }
    }

    public override string ToString()
    {
      var sb = new StringBuilder();
      sb.AppendFormat("Feed: {0}", Source);
      if (!string.IsNullOrEmpty(Username))
      {
        sb.AppendFormat("User: {0}, Password", Username);
      }
      return sb.ToString();
    }
  }

  public class NuGetSourceComparer : IEqualityComparer<INuGetSource>
  {
    public static readonly IEqualityComparer<INuGetSource> Comparer = new NuGetSourceComparer();
    private readonly IEqualityComparer<String> SourceComparer = StringComparer.InvariantCultureIgnoreCase;

    public bool Equals(INuGetSource x, INuGetSource y)
    {
      if (x == null && y == null) return true;
      if (x == null || y == null) return false;

      if (!SourceComparer.Equals(x.Source, y.Source)) return false;
      if (x.Username != y.Username) return false;
      if (x.Password != y.Password) return false;
      return true;
    }

    public int GetHashCode(INuGetSource obj)
    {
      if (obj == null) return 0;
      return SourceComparer.GetHashCode(obj.Source);
    }
  }
}
