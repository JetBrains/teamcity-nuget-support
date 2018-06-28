using System.IO;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public static class XmlSerializerHelper
  {
    public static T Load<T>(string path)
    {
      if (!File.Exists(path))
        throw new IOException("Failed to find file at " + path);

      using (var file = File.OpenRead(path))
      {
        var parser = new XmlSerializer(typeof(T));
        return (T)parser.Deserialize(file);
      }
    }

    public static void Save<T>(string path, T data)
    {
      if (!File.Exists(path))
        throw new IOException("Failed to find file at " + path);

      using (var file = File.CreateText(path))
      {
        var parser = new XmlSerializer(typeof(T));
        parser.Serialize(file, data);
      }
    }
  }
}
