using System.Linq;
using System.Linq.Expressions;
using JetBrains.TeamCity.NuGet.Feed.Query;
using JetBrains.TeamCity.NuGet.Feed.Query.Tree;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackagesQueryProvider : TCQueryProvider<TeamCityPackage>
  {
    private readonly PackageStore myStore;
    public PackagesQueryProvider(PackageStore store) : base(new TeamCityPackage[0].AsQueryable())
    {
      myStore = store;
    }

    protected override IQueryable<TElement> OptimizeWhereQuery<TElement>(Expression expression, FilterTreeNode tree)
    {
      if (!(typeof(TElement).Equals(typeof(TeamCityPackage)))) return base.OptimizeWhereQuery<TElement>(expression, tree);

      var idTree = tree.Normalize("Id");
      if (idTree is FilterUnknownTreeNode)
        return (IQueryable<TElement>) myStore.AllEntries().AsQueryable();

      var eq = idTree as FilterEqualsTreeNode;
      if (eq != null)
      {
        //Consider =L=
        return (IQueryable<TElement>) myStore.ForId(new[] {(string) eq.Value}).AsQueryable();
      }

      return base.OptimizeWhereQuery<TElement>(expression, tree);
    }
  }
}