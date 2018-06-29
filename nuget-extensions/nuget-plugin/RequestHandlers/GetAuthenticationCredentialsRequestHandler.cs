using System;
using System.Diagnostics;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles a <see cref="GetAuthenticationCredentialsRequest"/> and replies with credentials.
  /// </summary>
  internal class GetAuthenticationCredentialsRequestHandler
    : RequestHandlerBase<GetAuthenticationCredentialsRequest, GetAuthenticationCredentialsResponse>
  {
    private readonly ICredentialProvider _credentialProvider;

    /// <summary>
    /// Initializes a new instance of the <see cref="GetAuthenticationCredentialsRequestHandler"/> class.
    /// </summary>
    /// <param name="logger">A <see cref="TraceSource"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    public GetAuthenticationCredentialsRequestHandler(TraceSource logger, ICredentialProvider credentialProvider)
      : base(logger)
    {
      _credentialProvider = credentialProvider ?? throw new ArgumentNullException(nameof(credentialProvider));
    }

    public override GetAuthenticationCredentialsResponse HandleRequest(GetAuthenticationCredentialsRequest request)
    {
      try
      {
        var response = _credentialProvider.HandleRequest(request);
        if (response != null && response.ResponseCode == MessageResponseCode.Success)
        {
          return response;
        }
      }
      catch (Exception e)
      {
        Logger.Error($"Failed to acquire session token. {e}");

        return new GetAuthenticationCredentialsResponse(
          username: null,
          password: null,
          message: e.Message,
          authenticationTypes: null,
          responseCode: MessageResponseCode.Error);
      }

      return new GetAuthenticationCredentialsResponse(
        username: null,
        password: null,
        message: null,
        authenticationTypes: null,
        responseCode: MessageResponseCode.NotFound);
    }
  }
}
