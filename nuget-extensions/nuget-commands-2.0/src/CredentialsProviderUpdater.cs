using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
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

      return UpdateCredentialsProvider(new TeamCitySingleCredentialProvider(source));
    }

    public IDisposable UpdateCredentialsProvider([NotNull] IEnumerable<INuGetSource> source)
    {
      source = source.Where(x => x.HasCredentials).ToArray();
      if (source.IsEmpty()) return Noop;
      if (source.Count() == 1) return UpdateCredentialsProvider(source.First());

      return UpdateCredentialsProvider(new TeamCityMultipleCredentialProvider(source));
    }

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

    private static readonly IDisposable Noop = new DisposableDelegate(() => { });
  }
}
