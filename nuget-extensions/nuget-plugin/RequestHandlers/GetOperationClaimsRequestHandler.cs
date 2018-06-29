using System;
using System.Collections.Generic;
using System.Diagnostics;
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
    /// <param name="logger">A <see cref="TraceSource"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    public GetOperationClaimsRequestHandler(TraceSource logger, ICredentialProvider credentialProvider):base(logger)
    {
      myCredentialProvider = credentialProvider;
    }

    public override GetOperationClaimsResponse HandleRequest(GetOperationClaimsRequest request)
    {
      var operationClaims = new List<OperationClaim>();
      if (request.PackageSourceRepository == null && request.ServiceIndex == null ||
          Uri.TryCreate(request.PackageSourceRepository, UriKind.Absolute, out Uri uri) &&
          myCredentialProvider.CanProvideCredentials(uri))
      {
        operationClaims.Add(OperationClaim.Authentication);
      }

      return new GetOperationClaimsResponse(operationClaims);
    }
  }
}