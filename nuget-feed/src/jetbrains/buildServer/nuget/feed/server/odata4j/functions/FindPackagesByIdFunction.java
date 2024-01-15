

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.odata4j.ODataUtilities;
import jetbrains.buildServer.nuget.feed.server.odata4j.PackagesEntitySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.QueryInfo;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class FindPackagesByIdFunction implements NuGetFeedFunction {

  private final Logger LOG = Logger.getInstance(getClass().getName());
  private final NuGetFeed myFeed;

  public FindPackagesByIdFunction(@NotNull final NuGetFeed feed) {
    myFeed = feed;
  }

  @NotNull
  public String getName() {
    return MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME;
  }

  @NotNull
  public EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType) {
    return new EdmFunctionImport.Builder()
      .setName(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME)
      .setEntitySet(PackagesEntitySet.getBuilder())
      .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
      .setReturnType(returnType)
      .addParameters(new EdmFunctionParameter.Builder().setName(MetadataConstants.ID).setType(EdmSimpleType.STRING))
      .addParameters(new EdmFunctionParameter.Builder().setName(MetadataConstants.ID_UPPER_CASE).setType(EdmSimpleType.STRING));
  }

  @Nullable
  public Iterable<NuGetIndexEntry> call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo) {
    OFunctionParameter idParam = params.get(MetadataConstants.ID_UPPER_CASE);
    if (idParam == null) {
      idParam = params.get(MetadataConstants.ID);
      if (idParam == null) {
        LOG.debug(String.format("Bad %s function call. ID parameter is not specified.", getName()));
        return null;
      }
    }
    final OObject id = idParam.getValue();
    if (!(id instanceof OSimpleObject)) {
      LOG.debug(String.format("Bad %s function call. ID parameter type is invalid.", getName()));
      return null;
    }
    final OSimpleObject idObjectCasted = (OSimpleObject) id;
    final String packageId = idObjectCasted.getValue().toString();

    final boolean includeSemVer2 = ODataUtilities.includeSemVer2(queryInfo);
    return myFeed.findPackagesById(packageId, includeSemVer2);
  }
}
