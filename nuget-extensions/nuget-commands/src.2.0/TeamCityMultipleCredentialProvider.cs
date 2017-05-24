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
    private readonly IDictionary<Func<Uri, bool>, TeamCitySingleCredentialProvider> mySources;
    private readonly ICredentialProvider myNext;

    public TeamCityMultipleCredentialProvider(IEnumerable<INuGetSource> sources, ICredentialProvider next = null)
    {
      mySources = sources
        .Where(x => x.HasCredentials)
        .ToDictionary(
          x => UriEquals(x.Source),
          x => new TeamCitySingleCredentialProvider(x)
        );

      myNext = next;
    }

    public IDictionary<INuGetSource, ICredentials> Sources
    {
      get
      {
        var data = new Dictionary<INuGetSource, ICredentials>();
        //linq conversion of not allowed here as it may throw exception on key duplicate
        foreach (var pair in mySources.Values.SelectMany(x=>x.Sources))
        {
          data.Add(pair.Key, pair.Value);
        }
        return data;
      }
    }

    public ICredentials GetCredentials(Uri uri, IWebProxy proxy, CredentialType credentialType, bool retrying)
    {
      if (!retrying)
      {
        var matches = mySources
          .Where(x => x.Key(uri))
          .Select(x => x.Value.GetCredentials(uri, proxy, credentialType, false))
          .FirstOrDefault(x => x != null);

        if (matches != null) return matches;
      }

      if (myNext != null)
        return myNext.GetCredentials(uri, proxy, credentialType, retrying);

      return null;
    }

    private static Func<Uri, bool> UriEquals(string uri)
    {
      var uri1 = new Uri(uri.TrimEnd('/'));
      return uri2 =>
               {
                 uri2 = new Uri(uri2.OriginalString.TrimEnd('/'));
                 return Uri.Compare(uri1, uri2, UriComponents.SchemeAndServer | UriComponents.Path, UriFormat.SafeUnescaped, StringComparison.OrdinalIgnoreCase) == 0;
               };
    }
  }
}
