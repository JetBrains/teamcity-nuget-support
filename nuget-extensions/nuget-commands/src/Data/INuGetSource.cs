using System;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  public interface INuGetSource
  {
    [NotNull]
    String Source { get; }
    
    [CanBeNull]
    String Username { get; }
    
    [CanBeNull]
    String Password { get; }

    bool HasCredentials { get; }
  }
}
