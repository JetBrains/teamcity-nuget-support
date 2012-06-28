using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Util;
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

    protected IEnumerable<IPackage> GetAllPackages(string source, IEnumerable<string> ids)
    {
      IPackageRepository packageRepository = RepositoryFactory.CreateRepository(source);

      var param = Expression.Parameter(typeof (IPackageMetadata));

      var expressions = ids.Distinct().Select(id => Expression.Equal(Expression.Property(param, "Id"), Expression.Constant(id))).ToList();
      while (expressions.Count > 1)
      {
        var left = expressions.Where((x, i) => i%2 == 0).ToList();
        var right = expressions.Where((x, i) => i%2 == 1).ToList();
        expressions = ZipEx(left, right, Expression.Or).ToList();
      }

      var items = packageRepository.GetPackages();
      if (expressions.Count == 0) return items;
      return items.Where(Expression.Lambda<Func<IPackage, bool>>(expressions.Single(), param));
    }

    /// <summary>
    /// There was a change in signature in NuGet poset 1.5. IPackage.get_Version now returns Semantic version instead of Version
    /// Here is no difference here, so we can stick to dynamic object as there are tests for this method.
    /// </summary>    
    protected static void PrintPackageInfo(string id, string version)
    {    
      var msg = ServiceMessageFormatter.FormatMessage(
        "nuget-package",
        new ServiceMessageProperty("Id", id),
        new ServiceMessageProperty("Version", version)
        );

      System.Console.Out.WriteLine(msg);
    }
  }

  public static class PackageExtensions2
  {
    public static string VersionString(this IPackage package)
    {
      return ((dynamic) package).Version.ToString(); 
    }
  }
}