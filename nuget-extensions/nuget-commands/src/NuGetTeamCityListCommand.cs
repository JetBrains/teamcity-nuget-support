using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Linq.Expressions;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Util;
using NuGet;
using NuGet.Commands;
using NuGet.Common;
using PackageSourceProviderExtensions = NuGet.Common.PackageSourceProviderExtensions;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.List", "Lists packages for given Id with parsable output")]
  public class NuGetTeamCityListCommand : Command
  {
    [Import]
    public IPackageRepositoryFactory RepositoryFactory { get; set; }

    [Import]
    public IPackageSourceProvider SourceProvider { get; set; }

    [Option("NuGet package Source to search for package")]
    public String Source { get; set; }

    [Option("Package Id to check for version update")]
    public string Id { get; set; }

    [Option("NuGet Version Spec to constraint versions to be checked. Optional")]
    public string Version { get; set; }

    public override void ExecuteCommand()
    {
      System.Console.Out.WriteLine("TeamCity NuGet List command.");
      System.Console.Out.WriteLine("Source: {0}", Source ?? "<null>");
      System.Console.Out.WriteLine("Package Id: {0}", Id ?? "<null>");
      System.Console.Out.WriteLine("Version: {0}", Version ?? "<null>");
      
      System.Console.Out.WriteLine("Checking for latest version...");
      var packages = GetPackages();
      foreach (var p in packages)
      {
        var msg = ServiceMessageFormatter.FormatMessage(
          "nuget-package",
          new ServiceMessageProperty("Id", p.Id),
          new ServiceMessageProperty("Version", p.Version.ToString())          
          );

        System.Console.Out.WriteLine(msg);
      }
    }

    private IEnumerable<IPackage> GetPackages()
    {
      IPackageRepository packageRepository = RepositoryFactory.CreateRepository(Source);

      Expression<Func<IPackage, bool>> exp = p => p.Id == Id;
      IQueryable<IPackage> packages = packageRepository
        .GetPackages()
        .Where(exp);

      if (Version == null) return packages;
      return packages.Where(VersionUtility.ParseVersionSpec(Version).ToDelegate());
    }
  }
}
