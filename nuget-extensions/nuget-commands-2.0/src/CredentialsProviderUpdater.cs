using System;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public partial class CredentialsProviderUpdater
  {
    private static IDisposable UpdateCredentialsProvider([NotNull] ICredentialProvider provider)
    {

      try
      {
        //The only usage of credentials provider in NuGet source is to populate this static field, 
        //so it looks normal to hack-patch it.

        var originalProvider = HttpClient.DefaultCredentialProvider;

        HttpClient.DefaultCredentialProvider = provider;
        return new DisposableDelegate(
          () => { HttpClient.DefaultCredentialProvider = originalProvider; });
      }
      catch (Exception e)
      {
        Console.Out.WriteLine("Failed to update DefaultCredentialsProvider for HttpClient. " + e.Message);
        return Noop;
      }
    }
  }
}
