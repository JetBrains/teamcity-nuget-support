using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using NuGet.Common;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles a <see cref="GetOperationClaimsRequest"/> and replies with the supported operations.
  /// </summary>
  internal class GetOperationClaimsRequestHandler : RequestHandlerBase<GetOperationClaimsRequest, GetOperationClaimsResponse>
  {
    private readonly ICredentialProvider myCredentialProvider;

    /// <summary>
    /// Initializes a new instance of the <see cref="GetOperationClaimsRequestHandler"/> class.
    /// </summary>
    /// <param name="plugin">A <see cref="PluginController"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    public GetOperationClaimsRequestHandler(PluginController plugin, ICredentialProvider credentialProvider) : base(plugin)
    {
      myCredentialProvider = credentialProvider;
    }

    public override async Task<GetOperationClaimsResponse> HandleRequestAsync(GetOperationClaimsRequest request)
    {
      var operationClaims = new List<OperationClaim>();
      try
      {
        if (request.PackageSourceRepository == null && request.ServiceIndex == null ||
            Uri.TryCreate(request.PackageSourceRepository, UriKind.Absolute, out Uri uri) &&
            await myCredentialProvider.CanProvideCredentialsAsync(uri).ConfigureAwait(false))
        {
          operationClaims.Add(OperationClaim.Authentication);
        }
      }
      catch (Exception e)
      {
        await Plugin.LogMessageAsync(LogLevel.Error, $"Failed to execute credentials provider: {e}").ConfigureAwait(false);
      }

      return new GetOperationClaimsResponse(operationClaims);
    }
  }
}
