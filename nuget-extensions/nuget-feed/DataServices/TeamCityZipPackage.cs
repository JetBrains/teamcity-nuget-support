using System;
using System.IO;
using NuGet;
using NuGet.Server.DataServices;
using NuGet.Server.Infrastructure;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  public class TeamCityZipPackage : ZipPackage
  {
    private static readonly CryptoHashProvider HashProvider = new CryptoHashProvider();
    private readonly Lazy<DerivedPackageData> myDerivedData;

    public TeamCityZipPackage(string fileName) : base(fileName)
    {
      myDerivedData = new Lazy<DerivedPackageData>(() => CalculateDerivedData(fileName));
      Published = File.GetLastWriteTimeUtc(fileName);
    }

    public Package ToPackage
    {
      get { return new Package(this, myDerivedData.Value); }
    }

    private DerivedPackageData CalculateDerivedData(string path)
    {
      byte[] fileBytes;
      using (Stream stream = File.OpenRead(path))
      {
        fileBytes = stream.ReadAllBytes();
      }

      return new DerivedPackageData
               {
                 PackageSize = fileBytes.Length,
                 PackageHash = Convert.ToBase64String(HashProvider.CalculateHash(fileBytes)),
                 LastUpdated = File.GetLastWriteTimeUtc(path),
                 Created = File.GetCreationTimeUtc(path),
                 // TODO[nuget]: Add support when we can make this faster
                 // SupportedFrameworks = package.GetSupportedFrameworks(),

                 //TODO[jonny]: check returned path here
                 Path = path,        
                 FullPath = path
               };
    }
  }
}