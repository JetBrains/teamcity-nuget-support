using System;
using System.Collections.Generic;
using System.Net;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class TeamCitySingleCredentialProvider : ICredentialProvider, INuGetCredantialsProvider
  {
    private readonly INuGetSource mySource;
    private readonly ICredentialProvider myNext;

    public TeamCitySingleCredentialProvider(INuGetSource source, ICredentialProvider next = null)
    {
      mySource = source;
      myNext = next;
    }

    private ICredentials Credentials
    {
      get { return new NetworkCredential(mySource.Username, mySource.Password); }
    }

    public ICredentials GetCredentials(Uri uri, IWebProxy proxy, CredentialType credentialType, bool retrying)
    {
      if (!retrying && mySource.HasCredentials)
        return Credentials;

      if (myNext != null)
        return myNext.GetCredentials(uri, proxy, credentialType, retrying);

      return null;
    }

    public IDictionary<INuGetSource, ICredentials> Sources
    {
      get { return new Dictionary<INuGetSource, ICredentials> {{mySource, Credentials}}; }
    }
  }
}
