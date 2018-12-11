using System;
using System.Collections.Generic;
using System.IO;
using JetBrains.TeamCity.NuGet.ExtendedCommands;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet.Common;
using NuGet.Protocol.Plugins;
using ILogger = JetBrains.TeamCity.NuGet.Logging.ILogger;

namespace JetBrains.TeamCity.NuGet
{
  internal class TeamCityCredentialProvider : ICredentialProvider
  {
    private const string NugetFeedsEnv = "TEAMCITY_NUGET_FEEDS";
    private const string CouldProvideCredentialsForUriFromTheSource = "Could provide credentials for URI {0} from the source {1}";
    private const string CouldNotProvideCredentialsForUri = "Could not provide credentials for URI {0}";
    private const string FoundCredentialsForUriFromSource = "Found credentials for URI {0} from source {1}";
    private const string CredentialsForUriNotFound = "Credentials for URI {0} not found";
    private readonly ILogger _logger;

    internal TeamCityCredentialProvider(ILogger logger)
    {
      _logger = logger;

      var teamCityFeedsPath = Environment.GetEnvironmentVariable(NugetFeedsEnv);
      if (!string.IsNullOrEmpty(teamCityFeedsPath) && File.Exists(teamCityFeedsPath))
      {
        NuGetSources = XmlSerializerHelper.Load<NuGetSources>(teamCityFeedsPath);
      }
      else
      {
        NuGetSources = new NuGetSources();
      }
    }

    private INuGetSources NuGetSources { get; }

    public bool CanProvideCredentials(Uri uri)
    {
      var foundSource = NuGetSources.FindSource(uri);
      if (foundSource == null)
      {
        _logger.Log(LogLevel.Verbose, string.Format(CouldNotProvideCredentialsForUri, uri));
        return false;
      }

      _logger.Log(LogLevel.Verbose, string.Format(CouldProvideCredentialsForUriFromTheSource, uri, foundSource.Source));
      return true;
    }

    public GetAuthenticationCredentialsResponse HandleRequest(GetAuthenticationCredentialsRequest request)
    {
      var source = NuGetSources.FindSource(request.Uri);
      if (source != null)
      {
        _logger.Log(LogLevel.Verbose, string.Format(FoundCredentialsForUriFromSource, request.Uri, source.Source));
        return new GetAuthenticationCredentialsResponse(
          source.Username,
          source.Password,
          null,
          new List<string> {"basic"},
          MessageResponseCode.Success);
      }

      _logger.Log(LogLevel.Verbose, string.Format(CredentialsForUriNotFound, request.Uri));
      return new GetAuthenticationCredentialsResponse(null, null, null, null, MessageResponseCode.NotFound);
    }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public void Dispose()
    {
    }
  }
}
