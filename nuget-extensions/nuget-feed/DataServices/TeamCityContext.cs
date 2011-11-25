using System;
using System.Collections.Generic;
using System.Web;
using System.Web.Caching;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.Repo;
using JetBrains.TeamCity.ServiceMessages.Read;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public static class TeamCityContext
  {
    private static readonly Lazy<RepositoryPaths> myRepositoryPaths 
      = new Lazy<RepositoryPaths>(() => new RepositoryPaths(), true);

    private static readonly Lazy<ITeamCityServerAccessor> myAccessor 
      = new Lazy<ITeamCityServerAccessor>(() => new TeamCityServerAccessor(myRepositoryPaths.Value), true);

    private static readonly Lazy<PackageLoader> myLoader
      = new Lazy<PackageLoader>(() => new PackageLoader(), true);

    private static readonly Lazy<IServiceMessageParser> myParser
      = new Lazy<IServiceMessageParser>(() => new ServiceMessageParser(), true);

    private static LightPackageRepository CreateRepository([CanBeNull] string userId)
    {
      var remoteRepo = new RemoteRepo(
        myAccessor.Value.ForUser(userId),
        new PackagesDeserializer(myParser.Value, myLoader.Value)
        );

      return new LightPackageRepository(new CachedRepo(remoteRepo));
    }

    private static readonly Dictionary<string, TypedReference<LightPackageRepository>> myCache = new Dictionary<string, TypedReference<LightPackageRepository>>(); 

    public static LightPackageRepository GetRepository([CanBeNull] string userId)
    {
      string key = "Packages-" + (userId ?? "");
      
      lock(myCache)
      {
        TypedReference<LightPackageRepository> cached;
        if (myCache.TryGetValue(key, out cached))
        {
          var target = cached.Value;
          if (target != null)
            return  target;
        }

        var repo = CreateRepository(userId);
        myCache[key] = new TypedReference<LightPackageRepository>(repo);
        return repo;
      }
    }

    public static ITeamCityServerAccessor TeamCityAccessor
    {
      get { return myAccessor.Value; }
    }

    private struct TypedReference<T> where T : class
    {
      private readonly WeakReference myRefernce;

      public TypedReference(T refernce)
      {
        myRefernce = new WeakReference(refernce, true);
      }

      public T Value
      {
        get { return myRefernce.Target as T; }
      }
    }
  }
}