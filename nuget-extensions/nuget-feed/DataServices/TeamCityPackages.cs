using System;
using System.Data.Services;
using System.Data.Services.Common;
using System.Data.Services.Providers;
using System.IO;
using System.Linq;
using System.ServiceModel;
using System.Web;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [ServiceBehavior(IncludeExceptionDetailInFaults = true)]
  public class TeamCityPackages : DataService<TeamCityPackagesContext>, IDataServiceStreamProvider, IServiceProvider
  {    
    private readonly Func<IQueryable<TeamCityPackage>> myRepository;

    public TeamCityPackages()
    {
      myRepository = new LightPackageRepository().GetPackages;
    }

    // This method is called only once to initialize service-wide policies.
    [UsedImplicitly(ImplicitUseTargetFlags.Itself)]    
    public static void InitializeService(DataServiceConfiguration config)
    {
      config.SetEntitySetAccessRule("Packages", EntitySetRights.AllRead);
      config.SetEntitySetPageSize("Packages", 100);

      config.DataServiceBehavior.MaxProtocolVersion = DataServiceProtocolVersion.V2;
      config.UseVerboseErrors = true;
    }

    protected override TeamCityPackagesContext CreateDataSource()
    {
      return new TeamCityPackagesContext(myRepository);
    }

    public Uri GetReadStreamUri(object entity, DataServiceOperationContext operationContext)
    {
      var package = (TeamCityPackage)entity;
      return package.DownloadUrl;
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