using System;
using System.Linq;
using JetBrains.Annotations;
using NuGet.Server.DataServices;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class TeamCityPackagesContext
  {
    private readonly Func<IQueryable<Package>> myPackages;

    public TeamCityPackagesContext(Func<IQueryable<Package>> packages)
    {
      myPackages = packages;
    }

    [UsedImplicitly]
    public IQueryable<Package> Packages
    {
      get { return myPackages(); }
    } 
  }
}