// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using JetBrains.TeamCity.NuGet.Compatibility.Versioning;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace JetBrains.TeamCity.NuGet.Compatibility.Messages
{
  public static class MessageUtilities
  {
    private static readonly JsonSerializerSettings SerializerSettings = CreateSerializerSettings();

    public static Message Create<TPayload>(string requestId, MessageType type, MessageMethod method, TPayload payload)
      where TPayload : class
    {
      return new Message(requestId, type, method, JObject.FromObject(payload, CreateSerializer()));
    }

    public static TPayload DeserializePayload<TPayload>(Message message)
    {
      return message.Payload == null ? default(TPayload) : message.Payload.ToObject<TPayload>(CreateSerializer());
    }

    internal static string SerializeMessage(Message message)
    {
      return JsonConvert.SerializeObject(message, Formatting.None, SerializerSettings);
    }

    internal static Message DeserializeMessage(string line)
    {
      return JsonConvert.DeserializeObject<Message>(line, SerializerSettings);
    }

    private static JsonSerializer CreateSerializer()
    {
      return JsonSerializer.Create(SerializerSettings);
    }

    private static JsonSerializerSettings CreateSerializerSettings()
    {
      var settings = new JsonSerializerSettings();
      settings.Converters.Add(new SemanticVersionJsonConverter());
      return settings;
    }
  }
}
