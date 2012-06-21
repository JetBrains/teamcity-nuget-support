using System.IO;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class XmlWooDoo
  {
    public static string ToXml<T>(this T t)
    {
      var ser = new XmlSerializerFactory().CreateSerializer(typeof(T));
      var sw = new StringWriter();
      ser.Serialize(sw, t);
      return sw.ToString();
    }
  }
}