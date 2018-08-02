using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.ExtendedCommands;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet.Common;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet
{
  internal class TeamCityCredentialProvider : ICredentialProvider
  {
    private const string NugetFeedsEnv = "TEAMCITY_NUGET_FEEDS";
    private const string CouldProvideCredentialsForUriFromTheSource = "Could provide credentials for URI {0} from the source {1}";
    private const string CouldNotProvideCredentialsForUri = "Could not provide credentials for URI {0}";
    private const string FoundCredentialsForUriFromSource = "Found credentials for URI {0} from source {1}";
    private const string CredentialsForUriNotFound = "Credentials for URI {0} not found";
    private readonly PluginController _plugin;

    internal TeamCityCredentialProvider(PluginController plugin)
    {
      _plugin = plugin;

      var teamCityFeedsPath = Environment.GetEnvironmentVariable(NugetFeedsEnv);
      var nugetSources = new List<INuGetSource>();
      if (!string.IsNullOrEmpty(teamCityFeedsPath) && File.Exists(teamCityFeedsPath))
      {
        INuGetSources sources = XmlSerializerHelper.Load<NuGetSources>(teamCityFeedsPath);
        nugetSources.AddRange(sources.Sources.Where(x => x.HasCredentials));
      }

      NuGetSources = nugetSources;
    }

    private List<INuGetSource> NuGetSources { get; }

    public async Task<bool> CanProvideCredentialsAsync(Uri uri)
    {
      var foundSource = GetSource(uri);
      if (foundSource == null)
      {
        await _plugin.LogMessageAsync(LogLevel.Debug, string.Format(CouldNotProvideCredentialsForUri, uri));
        return false;
      }

      await _plugin.LogMessageAsync(LogLevel.Debug, string.Format(CouldProvideCredentialsForUriFromTheSource, uri, foundSource.Source));
      return true;
    }

    public async Task<GetAuthenticationCredentialsResponse> HandleRequestAsync(GetAuthenticationCredentialsRequest request)
    {
      var source = GetSource(request.Uri);
      if (source != null)
      {
        await _plugin.LogMessageAsync(LogLevel.Debug, string.Format(FoundCredentialsForUriFromSource, request.Uri, source.Source));
        return new GetAuthenticationCredentialsResponse(
          source.Username,
          source.Password,
          null,
          new List<string> {"basic"},
          MessageResponseCode.Success);
      }

      await _plugin.LogMessageAsync(LogLevel.Debug, string.Format(CredentialsForUriNotFound, request.Uri));
      return new GetAuthenticationCredentialsResponse(null, null, null, null, MessageResponseCode.NotFound);
    }

    private INuGetSource GetSource(Uri uri)
    {
      var requestUrl = uri.AbsoluteUri;
      if (!requestUrl.EndsWith("/"))
      {
        requestUrl += "/";
      }
      
      return NuGetSources.FirstOrDefault(x => requestUrl.StartsWith(x.Source, StringComparison.OrdinalIgnoreCase));
    }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public void Dispose()
    {
    }
  }
}
