// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace JetBrains.TeamCity.NuGet.Compatibility.Messages
{
  public sealed class Message
  {
    [JsonConstructor]
    public Message(string requestId, MessageType type, MessageMethod method, JObject payload = null)
    {
      RequestId = requestId;
      Type = type;
      Method = method;
      Payload = payload;
    }

    [JsonRequired]
    public string RequestId { get; }
    [JsonRequired]
    public MessageType Type { get; }
    [JsonRequired]
    public MessageMethod Method { get; }
    public JObject Payload { get; }
  }
}
