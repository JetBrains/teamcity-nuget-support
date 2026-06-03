// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class SetLogLevelRequest
  {
    [JsonConstructor]
    public SetLogLevelRequest(LogLevel logLevel)
    {
      LogLevel = logLevel;
    }

    [JsonRequired]
    public LogLevel LogLevel { get; }
  }
}
