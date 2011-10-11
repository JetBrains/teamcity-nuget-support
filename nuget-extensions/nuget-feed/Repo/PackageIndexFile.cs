using System.IO;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
{
  public class PackageIndexFile
  {
    private static readonly XmlSerializerFactory myFactory = new XmlSerializerFactory();
    private readonly XmlSerializer mySerializer;
    private readonly string myFile;

    public PackageIndexFile(string file)
    {
      myFile = file;
      mySerializer = myFactory.CreateSerializer(typeof (PackageIndex));
    }

    public PackageIndex Load()
    {
      try
      {
        using (var s = File.Open(myFile, FileMode.Open))
        {
          return (PackageIndex) mySerializer.Deserialize(s);
        }
      } catch
      {
        //Index corrupted
        return new PackageIndex();
      }
    }

    public void Save(PackageIndex index)
    {
      var tmpFile = myFile + ".1";
      try
      {        
        using(var s = File.Create(tmpFile))
        {
          mySerializer.Serialize(s, index);
        }
        File.Delete(myFile);
        File.Move(tmpFile, myFile);
      } catch
      {
        if (File.Exists(tmpFile))
        {
          try
          {
            File.Delete(tmpFile);
          } catch
          {
            //NOP
          }
        }
      }
    }
  }
}