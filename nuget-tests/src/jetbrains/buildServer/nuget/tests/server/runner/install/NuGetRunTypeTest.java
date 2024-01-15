

package jetbrains.buildServer.nuget.tests.server.runner.install;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.runner.NuGetRunType;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.03.12 18:54
 */
public abstract class NuGetRunTypeTest<TRunType extends NuGetRunType> extends BaseTestCase {
  protected Mockery m;
  protected PluginDescriptor myDescriptor;
  protected ServerToolManager myToolManager;
  protected ProjectManager myProjectManager;
  protected TRunType myRunType;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myDescriptor = m.mock(PluginDescriptor.class);
    myToolManager = m.mock(ServerToolManager.class);
    myProjectManager = m.mock(ProjectManager.class);
    myRunType = createRunType();
  }

  @NotNull
  protected abstract TRunType createRunType();

  protected void doTestValidator(@NotNull Map<String, String> parameters, @NotNull Set<String> errors) {
    Collection<InvalidProperty> errs = myRunType.getRunnerPropertiesProcessor().process(parameters);

    Set<String> actualErrors = new TreeSet<String>();
    for (InvalidProperty err : errs) {
      actualErrors.add(err.getPropertyName());
    }

    Assert.assertEquals(new TreeSet<String>(errors), actualErrors);
  }

  @NotNull
  protected Map<String, String> m(String... kvs) {
    Map<String, String> m = new TreeMap<String, String>();
    for(int i = 0; i < kvs.length; i+=2) {
      m.put(kvs[i], kvs[i+1]);
    }
    return m;
  }

  @NotNull
  protected Set<String> s(String... kvs) {
    Set<String> m = new TreeSet<String>();
    Collections.addAll(m, kvs);
    return m;
  }

}
