// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using JetBrains.TeamCity.NuGet.Compatibility.Protocol;

namespace JetBrains.TeamCity.NuGet.Compatibility.Connectivity
{
  public interface IConnection : IDisposable
  {
    event EventHandler<ProtocolErrorEventArgs> Faulted;
    bool IsConnected { get; }

    Task<TInbound> SendRequestAndReceiveResponseAsync<TOutbound, TInbound>(MessageMethod method, TOutbound payload, CancellationToken cancellationToken)
      where TOutbound : class
      where TInbound : class;
  }
}
