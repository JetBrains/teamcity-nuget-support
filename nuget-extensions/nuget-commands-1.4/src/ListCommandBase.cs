using System;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  public abstract partial class ListCommandBase
  {
    private void GetPackageRepository(INuGetSource source, Action<IPackageRepository> processor)
    {
      processor(RepositoryFactory.CreateRepository(source.Source));      
    }
  }
}
