// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class ProtocolErrorEventArgs : EventArgs
  {
    public ProtocolErrorEventArgs(Exception exception, Message message)
    {
      Exception = exception;
      Message = message;
    }

    public Exception Exception { get; }
    public Message Message { get; }
  }
}
