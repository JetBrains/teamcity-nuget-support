using System;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet
{
  /// <summary>
  /// Represents an interface for implementation of credential providers.
  /// </summary>
  internal interface ICredentialProvider : IDisposable
  {
    /// <summary>
    /// Determines whether this implementation can provide credentials for the specified <see cref="Uri"/>.
    /// </summary>
    /// <param name="uri">The <see cref="Uri"/> of the package feed.</param>
    /// <returns><code>true</code> if this implementation can provide credentials, otherwise <code>false</code>.</returns>
    bool CanProvideCredentials(Uri uri);

    /// <summary>
    /// Handles a <see cref="GetAuthenticationCredentialsRequest"/>.
    /// </summary>
    /// <param name="request">A <see cref="GetAuthenticationCredentialsRequest"/> object containing details about the request.</param>
    /// <returns>A <see cref="GetAuthenticationCredentialsResponse"/> object containing details about a response.</returns>
    GetAuthenticationCredentialsResponse HandleRequest(GetAuthenticationCredentialsRequest request);
  }
}
