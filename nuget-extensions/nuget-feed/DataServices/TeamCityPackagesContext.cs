using System;
using System.Collections;
using System.Linq;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
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