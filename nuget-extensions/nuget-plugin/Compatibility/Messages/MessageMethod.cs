// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.


namespace JetBrains.TeamCity.NuGet.Compatibility.Messages
{
  public enum MessageMethod
  {
    None = 0,
    Close = 1,
    GetOperationClaims = 6,
    Handshake = 10,
    Initialize = 11,
    Log = 12,
    MonitorNuGetProcessExit = 13,
    SetCredentials = 15,
    SetLogLevel = 16,
    GetAuthenticationCredentials = 17
  }
}
