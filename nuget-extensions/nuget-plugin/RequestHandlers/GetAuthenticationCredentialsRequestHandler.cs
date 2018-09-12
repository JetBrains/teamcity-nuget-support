using System;
using System.Threading.Tasks;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

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
    /// <param name="logger">A <see cref="ILogger"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    public GetAuthenticationCredentialsRequestHandler(ILogger logger, ICredentialProvider credentialProvider)
      : base(logger)
    {
      _credentialProvider = credentialProvider ?? throw new ArgumentNullException(nameof(credentialProvider));
    }

    public override Task<GetAuthenticationCredentialsResponse> HandleRequestAsync(GetAuthenticationCredentialsRequest request)
    {
      try
      {
        var response = _credentialProvider.HandleRequest(request);
        if (response != null && response.ResponseCode == MessageResponseCode.Success)
        {
          return Task.FromResult(response);
        }
      }
      catch (Exception e)
      {
        Logger.Log(LogLevel.Error, $"Failed to acquire credentials: {e}");

        return Task.FromResult(new GetAuthenticationCredentialsResponse(
          username: null,
          password: null,
          message: e.Message,
          authenticationTypes: null,
          responseCode: MessageResponseCode.Error));
      }

      return Task.FromResult(new GetAuthenticationCredentialsResponse(
        username: null,
        password: null,
        message: null,
        authenticationTypes: null,
        responseCode: MessageResponseCode.NotFound));
    }
  }
}
