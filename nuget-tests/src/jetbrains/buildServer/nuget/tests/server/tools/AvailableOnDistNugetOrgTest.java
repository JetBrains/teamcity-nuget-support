

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.impl.AvailableOnDistNugetOrg;
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class AvailableOnDistNugetOrgTest extends BaseTestCase {

  private final AvailableOnDistNugetOrg myFetcher = new AvailableOnDistNugetOrg();

  @Test
  public void fetchAvailable() {
    final FetchAvailableToolsResult result = myFetcher.fetchAvailable();
    assertNotEmpty(result.getFetchedTools());
    assertEmpty(result.getErrors());
  }
}
