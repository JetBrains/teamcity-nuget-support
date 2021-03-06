﻿using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using NuGet.Versioning;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet.RequestHandlers
{
  /// <summary>
  /// Handles a <see cref="GetOperationClaimsRequest"/> and replies with the supported operations.
  /// </summary>
  internal class GetOperationClaimsRequestHandler : RequestHandlerBase<GetOperationClaimsRequest, GetOperationClaimsResponse>
  {
    private readonly ICredentialProvider myCredentialProvider;
    private readonly bool mySupportAuthentication;

    /// <summary>
    /// Initializes a new instance of the <see cref="GetOperationClaimsRequestHandler"/> class.
    /// </summary>
    /// <param name="logger">A <see cref="Logging.ILogger"/> to use for logging.</param>
    /// <param name="credentialProvider">An <see cref="ICredentialProvider"/> containing credential provider.</param>
    /// <param name="sdkInfo">Sdk info provider.</param>
    public GetOperationClaimsRequestHandler(ILogger logger, ICredentialProvider credentialProvider, SdkInfo sdkInfo) : base(logger)
    {
      myCredentialProvider = credentialProvider;
      var hasVersion = sdkInfo.TryGetSdkVersion(out var semanticVersion);
      logger.Log(LogLevel.Verbose, hasVersion ? $".NET SDK {semanticVersion} was detected." : ".NET SDK was not detected.");

      mySupportAuthentication = !hasVersion || semanticVersion >= new SemanticVersion(2, 1, 400);
      logger.Log(LogLevel.Verbose, mySupportAuthentication ? "Authentication is supported." : "Authentication not is supported.");
    }

    public override Task<GetOperationClaimsResponse> HandleRequestAsync(GetOperationClaimsRequest request)
    {
      var operationClaims = new List<OperationClaim>();
      try
      {
        if (mySupportAuthentication)
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

      return Task.FromResult(new GetOperationClaimsResponse(operationClaims));
    }
  }
}
