// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using System;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class InitializeRequest
  {
    [JsonConstructor]
    public InitializeRequest(string clientVersion, string culture, TimeSpan requestTimeout)
    {
      ClientVersion = clientVersion;
      Culture = culture;
      RequestTimeout = requestTimeout;
    }

    [JsonRequired]
    public string ClientVersion { get; }
    [JsonRequired]
    public string Culture { get; }
    [JsonRequired]
    public TimeSpan RequestTimeout { get; }
  }
}
