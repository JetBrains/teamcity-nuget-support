namespace JetBrains.TeamCity.NuGet.CredentialProvider
{
  public enum ExitCode
  {
    UnknownError = -1,
    Success = 0,
    ProviderNotApplicable = 1,
    Failure = 2,
  }
}