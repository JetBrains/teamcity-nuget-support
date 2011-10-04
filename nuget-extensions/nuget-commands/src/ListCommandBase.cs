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

    protected IEnumerable<IPackage> GetAllPackages(string source, IEnumerable<string> ids)
    {
      IPackageRepository packageRepository = RepositoryFactory.CreateRepository(source);

      var param = Expression.Parameter(typeof (IPackageMetadata));

      Expression result = ids
        .Select(id => Expression.Equal(Expression.Property(param, "Id"), Expression.Constant(id)))
        .Aggregate<BinaryExpression, Expression>(null, (current, action) => current != null ? Expression.Or(action, current) : action);

      return packageRepository.GetPackages().Where(Expression.Lambda<Func<IPackage, bool>>(result, param));
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

  public static class PackageExtensions
  {
    public static string VersionString(this IPackage package)
    {
      return ((dynamic) package).Version.ToString(); 
    }
  }
}