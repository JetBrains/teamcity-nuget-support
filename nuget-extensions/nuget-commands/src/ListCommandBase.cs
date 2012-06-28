using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract class ListCommandBase : CommandBase
  {
    [Import]
    public IPackageRepositoryFactory RepositoryFactory { get; set; }

    [Import]
    public IPackageSourceProvider SourceProvider { get; set; }


    protected IEnumerable<IPackage> GetAllPackages(string source, PackageFetchOption fetchOption, IEnumerable<string> ids)
    {
      var items = GetPackageRepository(source).GetPackages();

      var param = Expression.Parameter(typeof(IPackage));
      Expression filter = QueryBuilder.GenerateQuery(fetchOption, ids, param);      
      return items.Where(Expression.Lambda<Func<IPackage, bool>>(filter, param));
    }

    private IPackageRepository GetPackageRepository(string source)
    {
      return RepositoryFactory.CreateRepository(source);
    }
  }
}