// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class LogResponse
  {
    public LogResponse(MessageResponseCode responseCode)
    {
      ResponseCode = responseCode;
    }

    [JsonRequired]
    public MessageResponseCode ResponseCode { get; }
  }
}
