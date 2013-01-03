using System.Collections.Generic;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  public interface INuGetPackages
  {
    IEnumerable<INuGetPackage> Packages { get; }
    void ClearCheckResults();
  }
}