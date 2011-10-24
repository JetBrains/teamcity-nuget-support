using System;
using System.Linq;
using JetBrains.Annotations;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class TeamCityPackagesContext
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();  

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