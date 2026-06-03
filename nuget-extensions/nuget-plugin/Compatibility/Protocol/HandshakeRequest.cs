// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using JetBrains.TeamCity.NuGet.Compatibility.Versioning;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  internal sealed class HandshakeRequest
  {
    public HandshakeRequest(SemanticVersion protocolVersion, SemanticVersion minimumProtocolVersion)
    {
      ProtocolVersion = protocolVersion;
      MinimumProtocolVersion = minimumProtocolVersion;
    }

    [JsonRequired]
    public SemanticVersion ProtocolVersion { get; }
    [JsonRequired]
    public SemanticVersion MinimumProtocolVersion { get; }
  }
}
