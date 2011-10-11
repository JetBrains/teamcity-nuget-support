using System;
using System.Data.Services;
using System.Data.Services.Common;
using System.Data.Services.Providers;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Web;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.Feed.Repo;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [ServiceBehavior(IncludeExceptionDetailInFaults = true)]
  public class TeamCityPackages : DataService<TeamCityPackagesContext>, IDataServiceStreamProvider, IServiceProvider
  {    
    private LightPackageRepository Repository
    {
      get { return TeamCityContext.Repository; }
    }

    // This method is called only once to initialize service-wide policies.
    [UsedImplicitly(ImplicitUseTargetFlags.Itself)]    
    public static void InitializeService(DataServiceConfiguration config)
    {
      config.SetEntitySetAccessRule("Packages", EntitySetRights.AllRead);
      config.SetEntitySetPageSize("Packages", 100);
      config.SetServiceOperationAccessRule("Search", ServiceOperationRights.AllRead);

      config.DataServiceBehavior.MaxProtocolVersion = DataServiceProtocolVersion.V2;
      config.UseVerboseErrors = true;
    }

    [WebGet, UsedImplicitly(ImplicitUseTargetFlags.Itself)]
    public IQueryable<TeamCityPackage> Search(string searchTerm, string targetFramework)
    {
      var packages = Repository.GetPackages();

      if (string.IsNullOrWhiteSpace(searchTerm)) return packages;
      var terms = searchTerm.Split().Where(x=>x.Length > 0).ToArray();

      if (!terms.Any()) 
        return packages;

      var param = Expression.Parameter(typeof (TeamCityPackage));
      //TODO: support Description and Tags 
      var prop = Expression.Call(Expression.Property(param, "Id"), "ToLower", new Type[0]);

      return packages.Where(
        Expression.Lambda<Func<TeamCityPackage, bool>>(
          terms
            .Select(term => Expression.Equal(prop, Expression.Constant(term)))
            .Aggregate(Expression.OrElse),
          param
          )
        );
    }


    protected override TeamCityPackagesContext CreateDataSource()
    {
      return new TeamCityPackagesContext(Repository.GetPackages);
    }

    public Uri GetReadStreamUri(object entity, DataServiceOperationContext operationContext)
    {
      var package = (TeamCityPackage)entity;
      var context = HttpContext.Current;

      string header = context.Request.Headers["X-TeamCityUrl"];
      var rootUrl = string.IsNullOrWhiteSpace(header)
                      ? context.Request.Url.GetComponents(UriComponents.SchemeAndServer, UriFormat.Unescaped)
                      : header;

      return new Uri(new Uri(rootUrl), package.DownloadUrl);
    }

    public string GetStreamContentType(object entity, DataServiceOperationContext operationContext)
    {
      return "application/zip";
    }

    public string GetStreamETag(object entity, DataServiceOperationContext operationContext)
    {
      return null;
    }

    public Stream GetWriteStream(object entity, string etag, bool? checkETagForEquality, DataServiceOperationContext operationContext)
    {
      throw new NotSupportedException();
    }

    public string ResolveType(string entitySetName, DataServiceOperationContext operationContext)
    {
      throw new NotSupportedException();
    }

    public int StreamBufferSize
    {
      get { return 64000; }
    }

    public object GetService(Type serviceType)
    {
      if (serviceType == typeof (IDataServiceStreamProvider))
        return this;
      return null;
    }

    public void DeleteStream(object entity, DataServiceOperationContext operationContext)
    {
      throw new NotSupportedException();
    }

    public Stream GetReadStream(object entity, string etag, bool? checkETagForEquality, DataServiceOperationContext operationContext)
    {
      throw new NotSupportedException();
    }
  }
}