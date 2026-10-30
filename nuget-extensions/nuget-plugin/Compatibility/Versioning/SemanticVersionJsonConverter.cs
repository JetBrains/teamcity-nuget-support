// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;

namespace JetBrains.TeamCity.NuGet.Compatibility.Versioning
{
  internal sealed class SemanticVersionJsonConverter : JsonConverter
  {
    public override bool CanConvert(Type objectType)
    {
      return objectType == typeof(SemanticVersion);
    }

    public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
    {
      switch (reader.TokenType)
      {
        case JsonToken.Null:
          return null;
        case JsonToken.String:
          return SemanticVersion.Parse((string)reader.Value);
      }

      var obj = JObject.Load(reader);
      var major = obj["Major"]?.Value<int>() ?? 0;
      var minor = obj["Minor"]?.Value<int>() ?? 0;
      var patch = obj["Patch"]?.Value<int>() ?? 0;
      var release = obj["Release"]?.Value<string>();
      var metadata = obj["Metadata"]?.Value<string>();
      return new SemanticVersion(major, minor, patch, release, metadata);
    }

    public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
    {
      writer.WriteValue(value?.ToString());
    }
  }
}
