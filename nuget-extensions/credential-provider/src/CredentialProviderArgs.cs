using System;
using PowerArgs;

namespace JetBrains.TeamCity.NuGet.CredentialProvider
{
  public class CredentialProviderArgs
  {
    [ArgDescription("The package source URI for which credentials will be filled")]
    [ArgRequired]
    public Uri Uri { get; set; }

    [ArgDescription("If present, provider will not issue interactive prompts")]
    public bool NonInteractive { get; set; }

    [ArgDescription("Notifies the provider that this is a retry and the credentials were rejected on a previous attempt")]
    public bool IsRetry { get; set; }
  }
}