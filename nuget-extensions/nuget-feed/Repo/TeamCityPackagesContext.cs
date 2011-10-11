using System;
using System.Linq;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class TeamCityPackagesContext
  {
    private readonly Func<IQueryable<TeamCityPackage>> myPackages;

    public TeamCityPackagesContext(Func<IQueryable<TeamCityPackage>> packages)
    {
      myPackages = packages;
    }

    [UsedImplicitly]
    public IQueryable<TeamCityPackage> Packages
    {
      get { return myPackages(); }
    } 
  }
}