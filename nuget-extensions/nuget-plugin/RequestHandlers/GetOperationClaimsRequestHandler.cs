using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;
using JetBrains.TeamCity.NuGet.Compatibility.Versioning;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles a <see cref="GetOperationClaimsRequest"/> and replies with the supported operations.
  /// </summary>
  internal class GetOperationClaimsRequestHandler : RequestHandlerBase<GetOperationClaimsRequest, GetOperationClaimsResponse>
  {
    private readonly ICredentialProvider myCredentialProvider;
    private readonly Lazy<Task<bool>> mySupportAuthentication;
    

    /// <summary>
    /// Initializes a new instance of the <see cref="GetOperationClaimsRequestHandler"/> class.
    /// </summary>
    /// <param name="logger">A <see cref="Logging.ILogger"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    /// <param name="sdkInfo">Sdk info provider.</param>
    public GetOperationClaimsRequestHandler(ILogger logger, ICredentialProvider credentialProvider, SdkInfo sdkInfo) : base(logger)
    {
      myCredentialProvider = credentialProvider;
      mySupportAuthentication = new Lazy<Task<bool>>(() => GetSupportAuthentication(sdkInfo));
    }

    public override async Task<GetOperationClaimsResponse> HandleRequestAsync(GetOperationClaimsRequest request)
    {
      var operationClaims = new List<OperationClaim>();
      try
      {
        if (await mySupportAuthentication.Value)
        {
          if (request.PackageSourceRepository == null && request.ServiceIndex == null ||
              Uri.TryCreate(request.PackageSourceRepository, UriKind.Absolute, out Uri uri) &&
              myCredentialProvider.CanProvideCredentials(uri))
          {
            operationClaims.Add(OperationClaim.Authentication);            
          }
        }
      }
      catch (Exception e)
      {
        Logger.Log(LogLevel.Error, $"Failed to execute credentials provider: {e}");
      }

      return new GetOperationClaimsResponse(operationClaims);
    }

    private async Task<bool> GetSupportAuthentication(SdkInfo sdkInfo)
    {
      var semanticVersion = await sdkInfo.GetSdkVersion();
      Logger.Log(LogLevel.Verbose, semanticVersion.HasValue ? $".NET SDK {semanticVersion.Value.Version} was detected." : ".NET SDK was not detected.");

      var supportAuthentication = !semanticVersion.HasValue || semanticVersion.Value.Version >= new SemanticVersion(2, 1, 400);
      Logger.Log(LogLevel.Verbose, supportAuthentication ? "Authentication is supported." : "Authentication not is supported.");
      
      return supportAuthentication;
    }
  }
}
