using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  [Serializable, XmlRoot("entry")]
  public class PackageIndexEntry
  {
    [XmlIgnore]
    private readonly List<long> myIndexes = new List<long>();

    public string Id { get; set; }
    
    public long[] Indexes
    {
      get { return myIndexes.ToArray(); }
      set
      {
        myIndexes.Clear();
        if (value != null)
          myIndexes.AddRange(value);
      }
    }

    public void AddEntry(long v)
    {
      myIndexes.Sort();
      myIndexes.Add(v);
    }
  }
}