using System;
using System.Collections.Generic;
using System.Xml.Serialization;
using System.Linq;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  [Serializable, XmlRoot("indexes")]
  public class PackageIndex
  {
    [XmlIgnore]
    private readonly Dictionary<string, PackageIndexEntry> myCache = new Dictionary<string, PackageIndexEntry>();

    [XmlArray("entries")]
    public PackageIndexEntry[] Entries
    {
      get { return myCache.Values.ToArray(); }
      set
      {
        myCache.Clear();
        if (value != null)
        {
          foreach (var p in value)
          {
            myCache[p.Id] = p;
          }
        }
      }
    }

    [CanBeNull]
    public PackageIndexEntry FindById(string id)
    {
      if (string.IsNullOrWhiteSpace(id)) return null;
      PackageIndexEntry r;
      return myCache.TryGetValue(id, out r) ? r : null;
    }

    public void AddEntry(long offset, PackageEntry entry)
    {
      var id = entry.Package.Id;
      var ie = FindById(id);
      if (ie != null)
      {
        ie.AddEntry(offset);
      } else
      {
        ie = new PackageIndexEntry
               {
                 Id = id,
                 Indexes = new[]{ offset }
               };
        myCache[id] = ie;
      }
    }
  }
}