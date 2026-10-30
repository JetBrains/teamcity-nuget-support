// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class GetOperationClaimsRequest
  {
    [JsonConstructor]
    public GetOperationClaimsRequest(string packageSourceRepository, JObject serviceIndex)
    {
      PackageSourceRepository = packageSourceRepository;
      ServiceIndex = serviceIndex;
    }

    public string PackageSourceRepository { get; }
    public JObject ServiceIndex { get; }
  }
}
