using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Serialization;
using NuGet;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Command("TeamCity.ListPackages", "Lists packages for given xml list of packages")]
  public class NuGetTeamCityListPackagesCommand : ListCommandBase
  {
    [Option("Path to file containing package-version pairs to check for updates")]
    public string Request { get; set; }

    public override void ExecuteCommand()
    {
      var req = LoadRequests();

      IEnumerable<IPackage> package = GetAllPackages(req.Source, req.Packages.Select(x => x.Id)).ToList();
      var hash = new Dictionary<string, Func<IPackage, bool>>(
        req
          .Packages
          .Where(x => x.LoadVersionSpec() != null)
          .ToDictionary(x => x.Id, x => x.LoadVersionSpec())
        );

      package = package.Where(x =>
                                {
                                  Func<IPackage, bool> r;
                                  if (!hash.TryGetValue(x.Id, out r))
                                    return true;
                                  return r(x);
                                });

      foreach (var pkg in package)
      {
        PrintPackageInfo(pkg);
      }
    }

    private NuGetRequests LoadRequests()
    {
      if (!File.Exists(Request))
        throw new CommandLineException("Failed to find file at {0}", Request);

      using (var file = File.OpenRead(Request))
      {
        var parser = new XmlSerializerFactory().CreateSerializer(typeof (NuGetRequests));
        return (NuGetRequests) parser.Deserialize(file);
      }
    }
  }

  [Serializable]
  [XmlRoot("Request")]
  public class NuGetCheckRequest
  {
    private Func<IPackage, bool> myVersionSpec;

    [XmlAttribute("Id")]
    public string Id { get; set; }

    [XmlAttribute("Versions")]
    public string VersionSpec { get; set; }

    public Func<IPackage, bool> LoadVersionSpec()
    {
      return string.IsNullOrWhiteSpace(VersionSpec)
               ? null
               : myVersionSpec ?? (myVersionSpec = VersionUtility.ParseVersionSpec(VersionSpec).ToDelegate());
    }
  }

  [Serializable]
  [XmlRoot("NuGet-Request")]
  public class NuGetRequests
  {
    [XmlAttribute("Source")]
    public string Source { get; set; }

    [XmlArray("Requests")]
    [XmlArrayItem("Request")]
    public NuGetCheckRequest[] Packages { get; set; }
  }
}