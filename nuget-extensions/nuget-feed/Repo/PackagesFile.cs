using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackagesFile : IDisposable
  {
    private static readonly XmlSerializerFactory myFactory = new XmlSerializerFactory();
    private readonly Stream myStream;
    private readonly XmlSerializer mySerializer;

    public PackagesFile(string file)
    {
      myStream = File.Open(file, FileMode.OpenOrCreate, FileAccess.ReadWrite);
      mySerializer = myFactory.CreateSerializer(typeof (PackageEntry));
    }

    public PackageEntry Load(long offset)
    {
      myStream.Seek(offset, SeekOrigin.Begin);
      return LoadRaw();
    }

    private PackageEntry LoadRaw()
    {
      int sz = myStream.ReadByte() + (myStream.ReadByte() << 8);

      var buff = new byte[sz];
      int i = 0;
      while (i < buff.Length)
        i += myStream.Read(buff, i, buff.Length - i);

      var zip = new GZipStream(new MemoryStream(buff), CompressionMode.Decompress, true);
      return (PackageEntry) mySerializer.Deserialize(zip);
    }

    public IEnumerable<PackageEntry> ReadAll()
    {
      myStream.Seek(0, SeekOrigin.Begin);
      while(myStream.Position < myStream.Length)
      {
        yield return LoadRaw();
      }
    } 

    public long Save(PackageEntry entry)
    {
      var mos = new MemoryStream();
      var zip = new GZipStream(mos, CompressionMode.Compress);
      mySerializer.Serialize(zip, entry);
      zip.Flush();
      zip.Close();
      var data = mos.GetBuffer();
      var sz = SerializeSize(data.Length);

      myStream.Seek(0, SeekOrigin.End);
      var pos = myStream.Position;
      myStream.Write(sz, 0, sz.Length);
      myStream.Write(data, 0, data.Length);
      myStream.Flush();
      return pos;
    }

    public void Dispose()
    {
      myStream.Dispose();
    }

    private byte[] SerializeSize(int sz)
    {
      return new[]
               {
                 (byte)(sz & 0xff),
                 (byte)((sz >> 8) & 0xff),
               };
    }
  }
}