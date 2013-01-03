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
        throw new InvalidFeedUrlException(source, "Local path does not exist");
      }

      System.Console.Out.WriteLine("Checking packages on source: {0}", source);
      var items = GetPackageRepository(source).GetPackages();

      var param = Expression.Parameter(typeof (IPackage));
      Expression filter = QueryBuilder.GenerateQuery(fetchOption, ids, param);
      return items.Where(Expression.Lambda<Func<IPackage, bool>>(filter, param));
    }

    private IPackageRepository GetPackageRepository(string source)
    {
      return RepositoryFactory.CreateRepository(source);
    }
  }

  public class InvalidFeedUrlException : Exception
  {
    public InvalidFeedUrlException(string feedUrl, string message) : base(string.Format("Speficied feed URI \"{0}\" is invalid. {1}", feedUrl ?? "<null>", message))
    {
    }
  }
}
