using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract class ListCommandBase : CommandBase
  {
    [Import]
    public IPackageRepositoryFactory RepositoryFactory { get; set; }

    [Import]
    public IPackageSourceProvider SourceProvider { get; set; }

    private static IEnumerable<T> ZipEx<T>(IEnumerable<T> left, IEnumerable<T> right, Func<T, T, T> zip)
    {
      var enuLeft = left.GetEnumerator();
      var enuRight = right.GetEnumerator();

      while (enuLeft.MoveNext() && enuRight.MoveNext())
        yield return zip(enuLeft.Current, enuRight.Current);

      while (enuLeft.MoveNext()) yield return enuLeft.Current;
      while (enuRight.MoveNext()) yield return enuRight.Current;
    }

    protected IEnumerable<IPackage> GetAllPackages(string source, PackageFetchOption fetchOption, IEnumerable<string> ids)
    {
      var param = Expression.Parameter(typeof (IPackage));

      var items = GetPackageRepository(source).GetPackages();

      Expression filter = GenerateQuery(fetchOption, ids, param);      
      return items.Where(Expression.Lambda<Func<IPackage, bool>>(filter, param));
    }

    private IPackageRepository GetPackageRepository(string source)
    {
      return RepositoryFactory.CreateRepository(source);
    }

    [NotNull]
    private Expression GenerateQuery(PackageFetchOption fetchOption, IEnumerable<string> ids, ParameterExpression param)
    {
      var idFilter = GeneratePackageIdFilter(ids, param);
      switch(fetchOption)
      {
        case PackageFetchOption.IncludeAll:
          return idFilter;
        case PackageFetchOption.IncludeLatest:
          {
            var pi = typeof (IPackage).GetProperty("IsLatestVersion");
            if (pi == null) goto case PackageFetchOption.IncludeAll;
            return Expression.And(Expression.Property(param, pi), idFilter);
          }
        case PackageFetchOption.IncludeLatestAndPrerelease:
          {
            var pi = typeof(IPackage).GetProperty("IsAbsoluteLatestVersion");
            if (pi == null) goto case PackageFetchOption.IncludeAll;
            return Expression.And(Expression.Property(param, pi), idFilter);
          }
        default:
          throw new Exception("Unexpected PackageFetchOption: " + fetchOption);
      }
    }

    [NotNull]
    private static Expression GeneratePackageIdFilter(IEnumerable<string> ids, ParameterExpression param)
    {
      var pi = typeof(IPackageMetadata).GetProperty("Id");
      var expressions = ids
        .Distinct()
        .Select(id => Expression.Equal(Expression.Property(param, pi), Expression.Constant(id)))
        .ToList();

      while (expressions.Count > 1)
      {
        var left = expressions.Where((x, i) => i%2 == 0);
        var right = expressions.Where((x, i) => i%2 == 1);
        expressions = ZipEx(left, right, Expression.Or).ToList();
      }

      if (expressions.Count == 0) 
        return Expression.Constant(true);

      return expressions.Single();
    }
  }

  public enum PackageFetchOption
  {
    IncludeAll, 
    IncludeLatest,
    IncludeLatestAndPrerelease,
  }

  public static class PackageExtensions2
  {
    public static string VersionString(this IPackage package)
    {
      // There was a change in signature in NuGet poset 1.5. IPackage.get_Version now returns Semantic version instead of Version
      // Here is no difference here, so we can stick to dynamic object as there are tests for this method.
      return ((dynamic)package).Version.ToString(); 
    }
  }
}