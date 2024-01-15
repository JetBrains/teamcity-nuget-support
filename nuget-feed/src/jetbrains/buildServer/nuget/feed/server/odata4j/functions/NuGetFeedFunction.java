

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.QueryInfo;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public interface NuGetFeedFunction {
  @NotNull String getName();
  @NotNull EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType);
  @Nullable Iterable<NuGetIndexEntry> call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo);
}
