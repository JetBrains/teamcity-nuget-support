using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles a <see cref="GetOperationClaimsRequest"/> and replies with the supported operations.
  /// </summary>
  internal class
    GetOperationClaimsRequestHandler : RequestHandlerBase<GetOperationClaimsRequest, GetOperationClaimsResponse>
  {
    /// <summary>
    /// Initializes a new instance of the <see cref="GetOperationClaimsRequestHandler"/> class.
    /// </summary>
    /// <param name="logger">A <see cref="TraceSource"/> to use for logging.</param>
    public GetOperationClaimsRequestHandler(TraceSource logger)
      : base(logger)
    {
    }

    public override Task<GetOperationClaimsResponse> HandleRequestAsync(GetOperationClaimsRequest request)
    {
      var operationClaims = new List<OperationClaim>();

      if (request.PackageSourceRepository == null && request.ServiceIndex == null)
      {
        operationClaims.Add(OperationClaim.Authentication);
      }

      return Task.FromResult(new GetOperationClaimsResponse(operationClaims));
    }
  }
}
