// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class ProtocolException : Exception
  {
    public ProtocolException(string message)
      : base(message)
    {
    }

    public ProtocolException(string message, Exception innerException)
      : base(message, innerException)
    {
    }
  }
}
