using System;
using JetBrains.Annotations;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  public interface INuGetPackage
  {
    bool IncludePrerelease { get; }

    [NotNull]
    string Id { get; }
    string VersionSpec { get; }

    [NotNull]
    INuGetSource Feed { get;  }

    void AddEntry(NuGetPackageEntry entry);

    void AddError(string message);

    Func<SemanticVersion, bool> VersionChecker { get; }
  }
}
