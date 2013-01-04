using System;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Export]
  public partial class CredentialsSetter : ICreatableComponent
  {
    public void Initialize()
    {
      var path = Environment.GetEnvironmentVariable("TEAMCITY_NUGET_FEEDS");
      if (string.IsNullOrWhiteSpace(path)) return;

      if (!File.Exists(path))
      {
        Console.Out.WriteLine("Failed to load NuGet feed credentials file: " + path);
        return;
      }

      var sources = XmlSerializerHelper.Load<NuGetSources>(path);
      var actual = sources.Sources.Where(x => x.HasCredentials).ToArray();
      
      if (actual.Any())
      {
        UpdateCredentials(actual);
      }
    }
  }
}
