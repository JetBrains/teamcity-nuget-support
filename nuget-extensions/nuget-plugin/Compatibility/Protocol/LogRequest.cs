// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using JetBrains.TeamCity.NuGet.Compatibility.Logging;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class LogRequest
  {
    public LogRequest(LogLevel logLevel, string message)
    {
      LogLevel = logLevel;
      Message = message;
    }

    [JsonRequired]
    public LogLevel LogLevel { get; }
    [JsonRequired]
    public string Message { get; }
  }
}
