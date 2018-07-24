using System;
using System.Collections.Generic;
using System.Net;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using System.Linq;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class TeamCityMultipleCredentialProvider : ICredentialProvider, INuGetCredantialsProvider
  {
    private readonly List<INuGetSource> mySources;
    private readonly ICredentialProvider myNext;

    public TeamCityMultipleCredentialProvider(IEnumerable<INuGetSource> sources, ICredentialProvider next = null)
    {
      mySources = sources.Where(x => x.HasCredentials).ToList();

      myNext = next;
    }

    public IDictionary<INuGetSource, ICredentials> Sources
    {
      get
      {
        var data = new Dictionary<INuGetSource, ICredentials>();
        //linq conversion of not allowed here as it may throw exception on key duplicate
        foreach (var source in mySources)
        {
          data.Add(source, new NetworkCredential(source.Username, source.Password));
        }
        return data;
      }
    }

    public ICredentials GetCredentials(Uri uri, IWebProxy proxy, CredentialType credentialType, bool retrying)
    {
      if (!retrying)
      {
        var requestUrl = uri.AbsoluteUri;
        if (!requestUrl.EndsWith("/"))
        {
          requestUrl += "/";
        }
        
        var source = mySources.FirstOrDefault(x => requestUrl.StartsWith(x.Source, StringComparison.OrdinalIgnoreCase));
        if (source != null) return new NetworkCredential(source.Username, source.Password);
      }

      if (myNext != null)
        return myNext.GetCredentials(uri, proxy, credentialType, retrying);

      return null;
    }
  }
}
