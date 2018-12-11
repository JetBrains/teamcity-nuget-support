using System;
using System.Collections.Generic;
using JetBrains.Annotations;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  public interface INuGetSources
  {
    [NotNull]
    IEnumerable<INuGetSource> Sources { get; }

    [CanBeNull]
    INuGetSource FindSource(Uri requestUri);
  }
}
