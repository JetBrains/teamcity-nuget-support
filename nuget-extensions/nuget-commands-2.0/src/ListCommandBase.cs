using System;
using System.Net;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract partial class ListCommandBase
  {
    private void GetPackageRepository(INuGetSource source, Action<IPackageRepository> processor)
    {
      var dispose = UpdateCredentialsProvider(source);
      try
      {
        processor(RepositoryFactory.CreateRepository(source.Source));
      }
      finally
      {
        dispose();
      }
    }

    private static Action UpdateCredentialsProvider(INuGetSource source)
    {
      if (!source.HasCredentials) return Noop;

      try
      {
        //The only usage of credentials provider in NuGet source is to populate this static field, 
        //so it looks normal to hack-patch it.

        var originalProvider = HttpClient.DefaultCredentialProvider;
        HttpClient.DefaultCredentialProvider = new TeamCityCredentialProvider(source);
        return () =>
                 {
                   HttpClient.DefaultCredentialProvider = originalProvider;
                 };
      }
      catch (Exception e)
      {
        System.Console.Out.WriteLine("Failed to update DefaultCredentialsProvider for HttpClient. " + e.Message);
        return Noop;
      }
    }

    private static void Noop()
    {
      
    }

    private class TeamCityCredentialProvider : ICredentialProvider
    {
      private readonly INuGetSource mySource;

      public TeamCityCredentialProvider(INuGetSource source)
      {
        mySource = source;
      }

      public ICredentials GetCredentials(Uri uri, IWebProxy proxy, CredentialType credentialType, bool retrying)
      {
        if (retrying) return null;

        if (String.IsNullOrWhiteSpace(mySource.Username)) return null;
        return new NetworkCredential(mySource.Username, mySource.Password);
      }
    }
  }
}
