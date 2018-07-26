package jetbrains.buildServer.nuget.common.version;

import org.jetbrains.annotations.NotNull;

public interface PackageVersion extends Comparable<PackageVersion> {
  @NotNull
  SemVerLevel getLevel();
}
