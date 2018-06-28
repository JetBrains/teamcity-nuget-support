#region LICENSE

// /*
//  * Copyright 2000-2018 JetBrains s.r.o.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

#endregion

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.ExtendedCommands;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet
{
  internal class TeamCityCredentialProvider : ICredentialProvider
  {
    private const string NugetFeedsEnv = "TEAMCITY_NUGET_FEEDS";

    private readonly GetAuthenticationCredentialsResponse NotFoundResponse =
      new GetAuthenticationCredentialsResponse(null, null, null, null, MessageResponseCode.NotFound);

    internal TeamCityCredentialProvider(TraceSource traceSource)
    {
      Logger = traceSource ?? new TraceSource(nameof(TeamCityCredentialProvider));

      var teamCityFeedsPath = Environment.GetEnvironmentVariable(NugetFeedsEnv);
      var nugetSources = new List<NuGetSource>();
      if (!string.IsNullOrEmpty(teamCityFeedsPath) && File.Exists(teamCityFeedsPath))
      {
        var sources = XmlSerializerHelper.Load<NuGetSources>(teamCityFeedsPath).Sources;
        if (sources != null)
        {
          nugetSources.AddRange(sources.Where(x => x.HasCredentials));
        }
      }

      NuGetSources = nugetSources;
    }

    /// <summary>
    /// Gets a <see cref="TraceSource"/> to use for logging.
    /// </summary>
    private TraceSource Logger { get; }

    /// <summary>
    /// Gets a <see cref="TraceSource"/> to use for logging.
    /// </summary>
    private List<NuGetSource> NuGetSources { get; }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public bool CanProvideCredentials(Uri uri)
    {
      return NuGetSources.Any(x => x.Source == uri.AbsoluteUri);
    }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public Task<GetAuthenticationCredentialsResponse> HandleRequestAsync(GetAuthenticationCredentialsRequest request,
      CancellationToken cancellationToken)
    {
      var source = NuGetSources.Where(x => x.HasCredentials).FirstOrDefault(x => x.Source == request.Uri.AbsoluteUri);
      if (source != null)
      {
        return Task.FromResult(new GetAuthenticationCredentialsResponse(
          source.Username,
          source.Password,
          null,
          new List<string> {"basic"},
          MessageResponseCode.Success));
      }

      return Task.FromResult(NotFoundResponse);
    }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public void Dispose()
    {
    }
  }
}
