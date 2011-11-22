using System;
using System.Collections.ObjectModel;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Web;
using log4net;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  /// <summary>
  /// from http://blog.maartenballiauw.be/post/2011/11/08/Rewriting-WCF-OData-Services-base-URL-with-load-balancing-reverse-proxy.aspx
  /// </summary>
  [AttributeUsage(AttributeTargets.Class)]
  public class RewriteBaseUrlBehavior : Attribute, IServiceBehavior
  {
    public void Validate(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase)
    {
      // Noop
    }

    public void AddBindingParameters(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase,
                                     Collection<ServiceEndpoint> endpoints, BindingParameterCollection bindingParameters)
    {
      // Noop
    }

    public void ApplyDispatchBehavior(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase)
    {
      foreach (ChannelDispatcher channelDispatcher in serviceHostBase.ChannelDispatchers)
      {
        foreach (EndpointDispatcher endpointDispatcher in channelDispatcher.Endpoints)
        {
          endpointDispatcher.DispatchRuntime.MessageInspectors.Add(new RewriteBaseUrlMessageInspector());
        }
      }
    }
  }

  /// <summary>
  /// from http://blog.maartenballiauw.be/post/2011/11/08/Rewriting-WCF-OData-Services-base-URL-with-load-balancing-reverse-proxy.aspx
  /// </summary>
  public class RewriteBaseUrlMessageInspector : IDispatchMessageInspector
  {
    private static readonly ILog LOG = LogManagerHelper.GetCurrentClassLogger();

    public object AfterReceiveRequest(ref Message request, IClientChannel channel, InstanceContext instanceContext)
    {
      var ctx = WebOperationContext.Current;
      if (ctx == null) return null;

      var req = ctx.IncomingRequest;
      if (req.UriTemplateMatch == null) return null;

      var baseUriBuilder = new UriBuilder(req.UriTemplateMatch.BaseUri);
      var requestUriBuilder = new UriBuilder(req.UriTemplateMatch.RequestUri);

      LOG.DebugFormat("Request: BaseUri : {0}, requestUri : {1}", baseUriBuilder, requestUriBuilder);

      var feed = req.Headers["X-TeamCityFeedBase"];
      if (feed == null) return null;

      var baseUri = new UriBuilder(feed);

      requestUriBuilder.Host = baseUri.Host;
      requestUriBuilder.Port = baseUri.Port;
      requestUriBuilder.Path = requestUriBuilder.Path.Replace("/" + NuGetRoutes.NUGET_FEED_ROUTE + "/", baseUri.Path.TrimEnd('/') + "/");
      
      
      OperationContext.Current.IncomingMessageProperties["MicrosoftDataServicesRootUri"] = baseUri.Uri;
      OperationContext.Current.IncomingMessageProperties["MicrosoftDataServicesRequestUri"] = requestUriBuilder.Uri;

      return null;
    }

    public void BeforeSendReply(ref Message reply, object correlationState)
    {
      // Noop
    }
  }
}