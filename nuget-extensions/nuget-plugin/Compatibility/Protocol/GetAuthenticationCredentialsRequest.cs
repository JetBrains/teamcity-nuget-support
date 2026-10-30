// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using System;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class GetAuthenticationCredentialsRequest
  {
    [JsonConstructor]
    public GetAuthenticationCredentialsRequest(Uri uri, bool isRetry, bool isNonInteractive, bool canShowDialog)
    {
      Uri = uri;
      IsRetry = isRetry;
      IsNonInteractive = isNonInteractive;
      CanShowDialog = canShowDialog;
    }

    [JsonRequired]
    public Uri Uri { get; }
    [JsonRequired]
    public bool IsRetry { get; }
    [JsonRequired]
    public bool IsNonInteractive { get; }
    [JsonRequired]
    public bool CanShowDialog { get; }
  }
}
