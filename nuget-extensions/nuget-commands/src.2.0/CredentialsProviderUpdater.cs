using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using System.Net;
using JetBrains.Annotations;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [Export]
  public partial class CredentialsProviderUpdater
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

      return UpdateCredentialsProvider(new TeamCityMultipleCredentialProvider(source));
    }

    private static readonly IDisposable Noop = new DisposableDelegate(() => { });
  }

  public interface INuGetCredantialsProvider
  {
    IDictionary<INuGetSource, ICredentials> Sources { get; } 
  }
}
