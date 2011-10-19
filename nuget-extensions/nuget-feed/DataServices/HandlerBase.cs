using System;
using System.Net;
using System.Web.Routing;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class HandlerBase
  {
    protected readonly RequestContext myContext;

    protected HandlerBase(RequestContext context)
    {
      myContext = context;
    }

    protected void WriteStatus(HttpStatusCode statusCode, string body)
    {
      myContext.HttpContext.Response.StatusCode = (int)statusCode;
      if (!String.IsNullOrEmpty(body))
        myContext.HttpContext.Response.Write(body);
    }
  }
}