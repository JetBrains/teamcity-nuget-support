using System;
using System.ComponentModel.Composition;
using System.IO;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Export]
  public partial class CredentialsSetter
  {
    [Import]
    public CredentialsProviderUpdater CredentialsUpdater { get; set; }

    public void Initialize()
    {
      var path = Environment.GetEnvironmentVariable("TEAMCITY_NUGET_FEEDS");
      if (string.IsNullOrWhiteSpace(path)) return;

      if (!File.Exists(path))
      {
        Console.Out.WriteLine("Failed to load NuGet feed credentials file: " + path);
      }

      var sources = XmlSerializerHelper.Load<NuGetSources>(path);

      CredentialsUpdater.UpdateCredentialsProvider(sources);


    }
  }
}
