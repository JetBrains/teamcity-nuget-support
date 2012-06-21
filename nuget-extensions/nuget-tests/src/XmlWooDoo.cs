using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class XmlWooDoo
  {
    public static string ToXml<T>(this T t)
    {
      var s = new XmlSerializer(typeof(T));
      var ms = new MemoryStream();
      using (var tw = new StreamWriter(ms, Encoding.UTF8))
        s.Serialize(tw, t);

      return new string(Encoding.UTF8.GetChars(ms.GetBuffer()));
    }

    public static XmlDocument LoadDocument(this string fileOut)
    {
      var doc = new XmlDocument();
      doc.Load(fileOut);
      return doc;
    }

    public static T LoadAsXml<T>(this string fileOut)
    {
      var ser = new XmlSerializerFactory().CreateSerializer(typeof(T));
      using (var s = File.OpenRead(fileOut))
        return ((T)ser.Deserialize(s));
    }

    public static void SaveAsXml<T>(this string fileOut, T obj)
    {
      var ser = new XmlSerializerFactory().CreateSerializer(typeof(T));
      using (var s = File.Create(fileOut))
        ser.Serialize(s, obj);
    }

    public static int XPathCount(this XmlDocument doc, string query)
    {
      var xmlNodeList = doc.SelectNodes(query);
      if (xmlNodeList == null) return 0;
      return xmlNodeList.Count;
    }
  }
}