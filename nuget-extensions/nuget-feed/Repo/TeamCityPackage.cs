using System;
using System.Data.Services.Common;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.Feed.Repo
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
    [NotNull]
    public string TeamCityDownloadUrl { get; set; }   

#region package provided properties
    [NotNull] public string Id { get; set; }
    [NotNull] public string Version { get; set; }
    [CanBeNull] public string ReleaseNotes { get; set; }
    [NotNull] public string Authors { get; set; }
    [CanBeNull] public string Dependencies { get; set; }
    [CanBeNull] public string Description { get; set; }
    [CanBeNull] public string Copyright { get; set; }
    [CanBeNull] public string ProjectUrl { get; set; }
    [CanBeNull] public string Tags { get; set; }
    [CanBeNull] public string IconUrl { get; set; }    
    [CanBeNull] public string LicenseUrl { get; set; }
    public bool RequireLicenseAcceptance { get; set; }
#endregion

    [NotNull] public string PackageHash { get; set; }
    [NotNull] public string PackageHashAlgorithm { get; set; }
    public long PackageSize { get; set; }
    public bool IsLatestVersion { get; set; }
    public DateTime LastUpdated { get; set; }
    

#region Properties that depegates to another properties
    public DateTime? Published { get { return LastUpdated; } }
    public DateTime Created { get { return LastUpdated; } }
    public string ExternalPackageUri { get { return ProjectUrl; } }
    public string GalleryDetailsUrl { get { return ProjectUrl; } }
    public string ReportAbuseUrl { get { return null; } }
    public string Summary { get { return Description; } }
    public string Title { get { return Id; } }
    public int DownloadCount { get { return 42; } }
    public int VersionDownloadCount { get { return 42; } }
    public string PacakgeProviderHost { get { return "JetBrains TeamCity"; } }
#endregion

#region obsolete propeties
    // TODO: remove these from the feed in the future, is possible, if they aren't used
    public string Categories { get { return string.Empty; } }
    public string Language { get { return ""; } }
    public string PackageType { get { return "Package"; } }
    public decimal Price { get { return 0; } }
#endregion
  }
}