using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

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
    /// <param name="logger">A <see cref="Logging.ILogger"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    public GetOperationClaimsRequestHandler(ILogger logger, ICredentialProvider credentialProvider) : base(logger)
    {
      myCredentialProvider = credentialProvider;
    }

    public override Task<GetOperationClaimsResponse> HandleRequestAsync(GetOperationClaimsRequest request)
    {
      var operationClaims = new List<OperationClaim>();
      try
      {
        if (request.PackageSourceRepository == null && request.ServiceIndex == null ||
            Uri.TryCreate(request.PackageSourceRepository, UriKind.Absolute, out Uri uri) &&
            myCredentialProvider.CanProvideCredentials(uri))
        {
          operationClaims.Add(OperationClaim.Authentication);
        }
      }
      catch (Exception e)
      {
        Logger.Log(LogLevel.Error, $"Failed to execute credentials provider: {e}");
      }

      return Task.FromResult(new GetOperationClaimsResponse(operationClaims));
    }
  }
}
