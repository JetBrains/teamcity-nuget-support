using System;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public partial class CredentialsProviderUpdater
  {
    private static IDisposable UpdateCredentialsProvider<T>([NotNull] T provider) where T : INuGetCredantialsProvider, ICredentialProvider
    {
      try
      {
        var store = (ICredentialCache)typeof(ICredentialCache).Assembly.GetType("NuGet.CredentialStore").GetProperty("Instance").GetValue(null, null);

        foreach (var url in provider.Sources)
        {
          store.Add(new Uri(url.Key.Source), url.Value);
        }
      }
      catch (Exception e)
      {
        Console.Out.WriteLine("Failed to update DefaultCredentialsProvider for HttpClient. " + e.Message);
      }
      return Noop;
    }
  }
}

