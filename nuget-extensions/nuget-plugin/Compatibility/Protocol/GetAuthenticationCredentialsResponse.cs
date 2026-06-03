// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using System.Collections.Generic;
using JetBrains.TeamCity.NuGet.Compatibility.Messages;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.Compatibility.Protocol
{
  public sealed class GetAuthenticationCredentialsResponse
  {
    [JsonConstructor]
    public GetAuthenticationCredentialsResponse(string username, string password, string message, IList<string> authenticationTypes, MessageResponseCode responseCode)
    {
      Username = username;
      Password = password;
      Message = message;
      AuthenticationTypes = authenticationTypes;
      ResponseCode = responseCode;
    }

    public string Username { get; }
    public string Password { get; }
    public string Message { get; }
    public IList<string> AuthenticationTypes { get; }
    [JsonRequired]
    public MessageResponseCode ResponseCode { get; }
  }
}
