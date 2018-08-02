using System;
using System.IO;
using System.Threading.Tasks;
using NuGet.Common;
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
    /// <param name="plugin">A <see cref="PluginController"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    public GetAuthenticationCredentialsRequestHandler(PluginController plugin, ICredentialProvider credentialProvider)
      : base(plugin)
    {
      _credentialProvider = credentialProvider ?? throw new ArgumentNullException(nameof(credentialProvider));
    }

    public override async Task<GetAuthenticationCredentialsResponse> HandleRequestAsync(GetAuthenticationCredentialsRequest request)
    {
      try
      {
        var response = await _credentialProvider.HandleRequestAsync(request);
        if (response != null && response.ResponseCode == MessageResponseCode.Success)
        {
          return response;
        }
      }
      catch (Exception e)
      {
        await Plugin.LogMessageAsync(LogLevel.Error, $"Failed to acquire credentials: {e}");

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
