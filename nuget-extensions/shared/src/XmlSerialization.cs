using System.IO;
using System.Xml.Serialization;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public static class XmlSerialization
  {
    private static XmlSerializer GetSerializer<T>()
    {
      var parser = new XmlSerializerFactory().CreateSerializer(typeof(T));
      if (parser == null)
        throw new CommandLineException("Failed to create serialized for parameters xml");

      return parser;
    }

    public static T LoadRequests<T>(string fileName)
    {
      if (!File.Exists(fileName))
        throw new CommandLineException("Failed to find file at {0}", fileName);

      using (var file = File.OpenRead(fileName))
      {
        return (T)GetSerializer<T>().Deserialize(file);
      }
    }

    public static void SaveRequests<T>(T obj, string fileName)
    {
      using (var file = File.CreateText(fileName))
      {
        GetSerializer<T>().Serialize(file, obj);
      }
    }
  }
}