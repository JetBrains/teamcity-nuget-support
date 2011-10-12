using System;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class XmlSerializers<T>
  {
    private static readonly Lazy<XmlSerializer> myLazy = new Lazy<XmlSerializer>(
      () => XmlSerializersImpl.myFactory.CreateSerializer(typeof(T)), true);

    public static XmlSerializer Create()
    {
      return myLazy.Value;
    }
  }

  static class XmlSerializersImpl
  {
    public static readonly XmlSerializerFactory myFactory = new XmlSerializerFactory();
  }
}