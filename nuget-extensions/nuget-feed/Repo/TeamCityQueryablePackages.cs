using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.Query;
using JetBrains.TeamCity.NuGet.Feed.Query.Tree;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class TeamCityQueryablePackages : TCQueryProvider<TeamCityPackage>
  {
    private readonly ITeamCityPackagesRepo myRepo;

    public TeamCityQueryablePackages(ITeamCityPackagesRepo repo) : base(repo.GetAllPackages().AsQueryable())
    {
      myRepo = repo;
    }

    protected override IQueryable<TeamCityPackage> OptimizeBasicWhereQuery(Expression<Func<TeamCityPackage, bool>> expression, FilterTreeNode node)
    {
      var idTree = node.Normalize("Id");
      var isLatestTree = node.Normalize("IsLatestVersion");

      var ids = ExtractIds(idTree);
      var isLatest = ExtractIsLatestVersion(isLatestTree);

      if (isLatest)
      {
        return myRepo.FiltetByIdLatest(ids).AsQueryable().Where(expression);
      }

      return myRepo.FilterById(ids).AsQueryable().Where(expression);
    }

    [CanBeNull]
    private IEnumerable<string> ExtractIds(FilterTreeNode node)
    {
      var ee = node as FilterEqualsTreeNode;
      if (ee != null && ee.Value is string)
        return new[] { (string)ee.Value };

      var or = node as FilterOrTreeNode;
      if (or != null)
      {
        var left = ExtractIds(or.Left);
        var right = ExtractIds(or.Right);

        if (left != null && right != null) return left.Union(right);
      }

      return null;
    }  

    private bool ExtractIsLatestVersion(FilterTreeNode node)
    {
      var eq = node as FilterEqualsTreeNode;
      return eq != null && eq.Value is bool && (bool) eq.Value;
    }
  }
}