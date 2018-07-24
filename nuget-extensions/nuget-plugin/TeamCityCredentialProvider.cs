﻿#region LICENSE

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
using JetBrains.TeamCity.NuGet.ExtendedCommands;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet.Protocol.Plugins;

namespace JetBrains.TeamCity.NuGet
{
  internal class TeamCityCredentialProvider : ICredentialProvider
  {
    private const string NugetFeedsEnv = "TEAMCITY_NUGET_FEEDS";

    internal TeamCityCredentialProvider(TraceSource traceSource)
    {
      Logger = traceSource ?? new TraceSource(nameof(TeamCityCredentialProvider));

      var teamCityFeedsPath = Environment.GetEnvironmentVariable(NugetFeedsEnv);
      var nugetSources = new List<INuGetSource>();
      if (!string.IsNullOrEmpty(teamCityFeedsPath) && File.Exists(teamCityFeedsPath))
      {
        INuGetSources sources = XmlSerializerHelper.Load<NuGetSources>(teamCityFeedsPath);
        nugetSources.AddRange(sources.Sources.Where(x => x.HasCredentials));
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
    private List<INuGetSource> NuGetSources { get; }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public bool CanProvideCredentials(Uri uri)
    {
      return GetSource(uri) != null;
    }

    /// <inheritdoc cref="IDisposable.Dispose"/>
    public GetAuthenticationCredentialsResponse HandleRequest(GetAuthenticationCredentialsRequest request)
    {
      var source = GetSource(request.Uri);
      if (source != null)
      {
        return new GetAuthenticationCredentialsResponse(
          source.Username,
          source.Password,
          null,
          new List<string> {"basic"},
          MessageResponseCode.Success);
      }

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
