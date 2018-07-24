using System.Collections.Generic;
using System.ComponentModel.Composition;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public partial class CredentialsSetter
  {
    [Import]
    public CredentialsProviderUpdater CredentialsUpdater { get; set; }

    private void UpdateCredentials(IEnumerable<INuGetSource> actual)
    {
      CredentialsUpdater.UpdateCredentialsProvider(actual);
    }
  }
}
