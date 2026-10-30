// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Versioning;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  internal sealed class HandshakeResponse
  {
    [JsonConstructor]
    public HandshakeResponse(MessageResponseCode responseCode, SemanticVersion protocolVersion)
    {
      ResponseCode = responseCode;
      ProtocolVersion = protocolVersion;
    }

    [JsonRequired]
    public MessageResponseCode ResponseCode { get; }
    public SemanticVersion ProtocolVersion { get; }
  }
}
