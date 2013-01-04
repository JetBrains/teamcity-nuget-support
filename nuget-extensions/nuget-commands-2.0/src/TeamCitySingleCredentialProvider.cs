using System;
using System.Net;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public class TeamCitySingleCredentialProvider : ICredentialProvider
  {
    private readonly INuGetSource mySource;
    private readonly ICredentialProvider myNext;

    public TeamCitySingleCredentialProvider(INuGetSource source, ICredentialProvider next = null)
    {
      mySource = source;
      myNext = next;
    }

    public ICredentials GetCredentials(Uri uri, IWebProxy proxy, CredentialType credentialType, bool retrying)
    {
      if (!retrying && !String.IsNullOrWhiteSpace(mySource.Username))
        return new NetworkCredential(mySource.Username, mySource.Password);

      if (myNext != null)
        return myNext.GetCredentials(uri, proxy, credentialType, retrying);

      return null;
    }
  }
}
