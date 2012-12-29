using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public static class QueryBuilder
  {
    [NotNull]
    public static Expression GenerateQuery(PackageFetchOption fetchOption, IEnumerable<string> ids, ParameterExpression param)
    {
      var idFilter = GeneratePackageIdFilter(ids, param);
      switch (fetchOption)
      {
        case PackageFetchOption.IncludeAll:
          return idFilter;
        case PackageFetchOption.IncludeLatest:
          {
            var pi = typeof(IPackage).GetProperty("IsLatestVersion");
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
    public static Expression GeneratePackageIdFilter(IEnumerable<string> ids, ParameterExpression param)
    {
      var toLower = typeof (string).GetMethod("ToLower", new Type[0]);
      var pi = typeof(IPackageMetadata).GetProperty("Id");
      var expressions = ids
        .Distinct()
        .Select(id => Expression.Equal(Expression.Call(Expression.Property(param, pi), toLower), Expression.Constant(id.ToLower())))
        .ToList();

      while (expressions.Count > 1)
      {
        var left = expressions.Where((x, i) => i % 2 == 0).ToList();
        var right = expressions.Where((x, i) => i % 2 == 1).ToList();
        expressions = ZipEx(left, right, Expression.Or).ToList();
      }

      if (expressions.Count == 0)
        return Expression.Constant(true);

      return expressions.Single();
    }

    private static IEnumerable<T> ZipEx<T>(IEnumerable<T> left, IEnumerable<T> right, Func<T, T, T> zip)
    {
      var enuLeft = left.GetEnumerator();
      var enuRight = right.GetEnumerator();

      bool hasLeft;
      bool hasRight;
      do
      {
        hasLeft = enuLeft.MoveNext();
        hasRight = enuRight.MoveNext();

        if (hasLeft && hasRight) yield return zip(enuLeft.Current, enuRight.Current);
        else if (hasLeft) yield return enuLeft.Current;
        else if (hasRight) yield return enuRight.Current;

      } while (hasLeft || hasRight);
    }
  }
}
