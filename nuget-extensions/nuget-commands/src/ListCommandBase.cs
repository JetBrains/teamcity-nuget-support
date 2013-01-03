using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract class ListCommandBase : CommandBase
  {
    [Import]
    public IPackageRepositoryFactory RepositoryFactory { get; set; }

    [Import]
    public IPackageSourceProvider SourceProvider { get; set; }

    /// <exception cref="InvalidFeedUrlException">may be thrown on error</exception>
    [NotNull]
    protected IEnumerable<IPackage> GetAllPackages(NuGetSource feed, PackageFetchOption fetchOption, IEnumerable<string> ids)
    {
      System.Console.Out.WriteLine("Checking packages on source: {0}", feed);

      ValidateSourceUrl(feed);
      var items = GetPackageRepository(feed).GetPackages();

      var param = Expression.Parameter(typeof (IPackage));
      Expression filter = QueryBuilder.GenerateQuery(fetchOption, ids, param);
      return items.Where(Expression.Lambda<Func<IPackage, bool>>(filter, param));
    }

    private static void ValidateSourceUrl(NuGetSource feed)
    {
      string source = feed.Source;
      Uri uri;
      try
      {
        uri = new Uri(source);
      }
      catch (Exception e)
      {
        throw new InvalidFeedUrlException(source, e.Message);
      }

      if (uri.IsFile && !Directory.Exists(uri.LocalPath))
      {
        throw new InvalidFeedUrlException(source, "Local path does not exist: " + uri.LocalPath);
      }
    }

    private IPackageRepository GetPackageRepository(NuGetSource source)
    {
      return RepositoryFactory.CreateRepository(source.Source);
    }
  }
}
