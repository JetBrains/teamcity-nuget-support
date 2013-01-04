using System;
using System.ComponentModel.Composition;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Export]
  public class CredentialsProviderUpdater
  {
    public IDisposable UpdateCredentialsProvider([NotNull] INuGetSource source)
    {
      if (!source.HasCredentials) return Noop;

      try
      {
        //The only usage of credentials provider in NuGet source is to populate this static field, 
        //so it looks normal to hack-patch it.

        var originalProvider = HttpClient.DefaultCredentialProvider;
        HttpClient.DefaultCredentialProvider = new TeamCityCredentialProvider(source);
        return new DisposableDelegate(
          () =>
            {
              HttpClient.DefaultCredentialProvider = originalProvider;
            });
      }
      catch (Exception e)
      {
        Console.Out.WriteLine("Failed to update DefaultCredentialsProvider for HttpClient. " + e.Message);
        return Noop;
      }
    }

    private static readonly IDisposable Noop = new DisposableDelegate(() => { });
  }
}
