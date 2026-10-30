// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class MonitorNuGetProcessExitRequest
  {
    [JsonConstructor]
    public MonitorNuGetProcessExitRequest(int processId)
    {
      ProcessId = processId;
    }

    [JsonRequired]
    public int ProcessId { get; }
  }
}
