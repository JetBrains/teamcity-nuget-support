// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public interface IResponseHandler
  {
    Task SendResponseAsync<TPayload>(Message request, TPayload payload, CancellationToken cancellationToken) where TPayload : class;
  }
}
