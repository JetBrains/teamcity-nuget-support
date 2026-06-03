// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class SetCredentialsRequest
  {
    [JsonConstructor]
    public SetCredentialsRequest(string packageSourceRepository, string proxyUsername, string proxyPassword, string username, string password)
    {
      PackageSourceRepository = packageSourceRepository;
      ProxyUsername = proxyUsername;
      ProxyPassword = proxyPassword;
      Username = username;
      Password = password;
    }

    [JsonRequired]
    public string PackageSourceRepository { get; }
    public string ProxyUsername { get; }
    public string ProxyPassword { get; }
    public string Username { get; }
    public string Password { get; }
  }
}
