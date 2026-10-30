// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using System.Collections.Generic;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class GetOperationClaimsResponse
  {
    [JsonConstructor]
    public GetOperationClaimsResponse(IEnumerable<OperationClaim> claims)
    {
      Claims = claims.ToList();
    }

    [JsonRequired]
    public IReadOnlyList<OperationClaim> Claims { get; }
  }
}
