package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;

import java.util.Map;
import java.util.Objects;

abstract class NuGetFeedFunctionBase implements NuGetFeedFunction {

  private final SemanticVersion VERSION_20 = Objects.requireNonNull(SemanticVersion.valueOf("2.0.0"));

  protected boolean includeSemVer2(@NotNull Map<String, OFunctionParameter> params) {
    boolean semVer20 = false;
    final OFunctionParameter semVerParam = params.get(MetadataConstants.SEMANTIC_VERSION);
    if (semVerParam != null) {
      final OObject semVerParamValue = semVerParam.getValue();
      if (semVerParamValue instanceof OSimpleObject) {
        final SemanticVersion version = SemanticVersion.valueOf(((OSimpleObject) semVerParamValue).getValue().toString());
        semVer20 = version != null && version.compareTo(VERSION_20) >= 0;
      }
    }
    return semVer20;
  }
}
