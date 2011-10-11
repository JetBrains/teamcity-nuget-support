using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackageStore
  {
    private readonly string myDataFile;
    private readonly string myIndexFile;

    private readonly PackageIndexFile myIndex;
    private readonly PackagesFile myPackages;

    private PackageIndex myCachedIndex;

    public PackageStore(string home)
    {
      myDataFile = Path.Combine(home, "data.jz");
      myIndexFile = Path.Combine(home, "index.jz");

      myIndex = new PackageIndexFile(myIndexFile);
      myPackages = new PackagesFile(myDataFile);
    }

    private PackageIndex Index
    {
      get { return myCachedIndex ?? (myCachedIndex = myIndex.Load()); }
    }

    public IEnumerable<PackageEntry> AllEntries()
    {
      return myPackages.ReadAll();
    }

    public IEnumerable<PackageEntry> ForId(IEnumerable<string> ids)
    {
      var entries = ids
        .Where(x => x.Length > 0)
        .Select(Index.FindById)
        .Where(x => x != null && x.Indexes.Any())
        .ToList();

      var firstPackages = entries.Select(x => x.Indexes.First()).ToList().OrderBy(x=>x);
      var otherPachages = entries.SelectMany(x => x.Indexes.Skip(1)).ToList().OrderBy(x=>x);

      return firstPackages
        .Union(otherPachages)
        .Select(myPackages.Load);
    }

    public void AddPackage(PackageEntry entry)
    {
      var offset = myPackages.Save(entry);
      var index = Index;

      index.AddEntry(offset, entry);      
      myIndex.Save(index);
    }

  }
}