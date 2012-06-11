using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public class NuGetRunner_ListPackagesCommandTestBase
  {
    [XmlRoot("nuget-packages")]
    public class NuGetPackages
    {
      [XmlArray("packages")]
      [XmlArrayItem("package")]
      public Package[] Packages { get; set; }
    }

    [XmlRoot("package")]
    public class Package
    {
      [XmlAttribute("source")]
      public string Source { get; set; }

      [XmlAttribute("id")]
      public string Id { get; set; }

      [XmlAttribute("versions")]
      public string Versions { get; set; }

      [XmlAttribute("include-prerelease")]
      public string IncludePrerelease { get; set; }
    }


    protected static string Serialize(IEnumerable<Package> pp)
    {
      return Serialize(pp.ToArray());
    }

    protected static string Serialize(params Package[] pp)
    {
      var s = new XmlSerializer(typeof (NuGetPackages));
      var ms = new MemoryStream();
      using(var tw = new StreamWriter(ms, Encoding.UTF8))
        s.Serialize(tw, new NuGetPackages {Packages = pp});

      return new string(Encoding.UTF8.GetChars(ms.GetBuffer()));
    }

    protected static Package p(string source, string id, string version = null, bool? includePrerelease = null)
    {
      return new Package
               {
                 Source = source,
                 Id = id,
                 Versions = version,
                 IncludePrerelease = includePrerelease == null ? null : includePrerelease.ToString()
               };
    }

    protected static Package p1(string id, string version = null, bool? includePrerelease = null)
    {
      return p(NuGetConstants.DefaultFeedUrl_v1, id, version, includePrerelease);
    }

    protected static Package p2(string id, string version = null, bool? includePrerelease = null)
    {
      return p(NuGetConstants.DefaultFeedUrl_v2, id, version, includePrerelease);
    }

    protected static int PackagesCount(XmlDocument doc, string id)
    {
      var p = doc.SelectNodes("//package[@id='" + id + "']//package-entry");
      return p == null ? 0 : p.Count;
    }

    protected static XmlDocument DoTestWithSpec(NuGetVersion version, string spec)
    {
      return TempFilesHolder.WithTempFile(
        fileOut =>
        TempFilesHolder.WithTempFile(
          fileIn =>
            {
              File.WriteAllText(fileIn, spec);

              ProcessExecutor.ExecuteProcess(Files.NuGetRunnerExe, Files.GetNuGetExe(version),
                                             "TeamCity.ListPackages", "-Request", fileIn, "-Response", fileOut)
                .Dump()
                .AssertExitedSuccessfully()
                ;

              Console.Out.WriteLine("Result: " + File.ReadAllText(fileOut));
              var doc = new XmlDocument();
              doc.Load(fileOut);
              return doc;
            }));
    }
  }
}