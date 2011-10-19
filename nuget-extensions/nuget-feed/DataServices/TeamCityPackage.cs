using System;
using System.Data.Services.Common;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.Feed.DataServices
{
  [HasStream]
  [Serializable]
  [DataServiceKey("Id", "Version")]
  [EntityPropertyMapping("Id", SyndicationItemProperty.Title, SyndicationTextContentKind.Plaintext, keepInContent: true)]
  [EntityPropertyMapping("Authors", SyndicationItemProperty.AuthorName, SyndicationTextContentKind.Plaintext, keepInContent: true)]
  [EntityPropertyMapping("LastUpdated", SyndicationItemProperty.Updated, SyndicationTextContentKind.Plaintext, keepInContent: true)]
  [EntityPropertyMapping("Summary", SyndicationItemProperty.Summary, SyndicationTextContentKind.Plaintext, keepInContent: true)]  
  public class TeamCityPackage
  {
    internal string DownloadUrl { get; set; }
    public string PacakgeProviderHost { get { return "JetBrains TeamCity"; } set { } }


    public string Id { get; set; }
    public string Version { get; set; }

    public string Authors { get; set; }
    public string Copyright { get; set; }
    public DateTime Created { get; set; }
    public string Dependencies { get; set; }
    public string Description { get; set; }    
    public string ExternalPackageUri { get; set; }
    public string GalleryDetailsUrl { get; set; }
    public string IconUrl { get; set; }
    public bool IsLatestVersion { get; set; }
    public DateTime LastUpdated { get; set; }
    public string LicenseUrl { get; set; }
    public string PackageHash { get; set; }
    public string PackageHashAlgorithm { get; set; }
    public long PackageSize { get; set; }
    public string ProjectUrl { get; set; }
    public DateTime? Published { get; set; }
    public string ReportAbuseUrl { get; set; }
    public bool RequireLicenseAcceptance { get; set; }
    public string Summary { get; set; }
    public string Tags { get; set; }
    public string Title { get; set; }

    public int DownloadCount { get { return 42; } }
    public int VersionDownloadCount { get { return 42; } }

    // TODO: remove these from the feed in the future, is possible, if they aren't used
    public string Categories { get { return string.Empty; } }
    public string Language { get { return ""; } }
    public string PackageType { get { return "Package"; } }
    public decimal Price { get { return 0; } }

    public string ReleaseNotes { get; set; }
  }
}