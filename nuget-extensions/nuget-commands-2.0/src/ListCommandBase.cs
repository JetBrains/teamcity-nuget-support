using System;
using System.ComponentModel.Composition;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract partial class ListCommandBase
  {
    [Import]
    public CredentialsProviderUpdater CredentialsUpdater { get; set; }

    private void GetPackageRepository(INuGetSource source, Action<IPackageRepository> processor)
    {
      using(CredentialsUpdater.UpdateCredentialsProvider(source))
      {
        processor(RepositoryFactoryTC.CreateRepository(source.Source));
      }      
    }
  }
}
