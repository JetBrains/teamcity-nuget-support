using System;
using System.IO;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public static class XmlSerializerHelper
  {
    public static T Load<T>(string path)
    {
      if (!File.Exists(path))
        throw new ApplicationException("Failed to find file at " + path);

      using (var file = File.OpenRead(path))
      {
        var parser = new XmlSerializerFactory().CreateSerializer(typeof(T));
        if (parser == null)
          throw new ApplicationException("Failed to create serialized for " + typeof(T).FullName);

        return (T)parser.Deserialize(file);
      }
    }

    public static void Save<T>(string path, T data)
    {
      if (!File.Exists(path))
        throw new ApplicationException("Failed to find file at " + path);

      using (var file = File.CreateText(path))
      {
        var parser = new XmlSerializerFactory().CreateSerializer(typeof(T));
        if (parser == null)
          throw new ApplicationException("Failed to create serialized for " + typeof(T).FullName);

        parser.Serialize(file, data);
      }
    }
  }
}
