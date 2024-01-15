

package jetbrains.buildServer.nuget.tests;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 21:18
 */
public class LoggingTestCase extends BaseTestCase {
  private List<String> myLog;

  protected synchronized void log(@NotNull String message) {
    myLog.add(message);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myLog = new ArrayList<String>();
  }

  protected void assertLog(String... gold) {
    String actual = StringUtil.join(myLog, "\n");
    String expected = StringUtil.join(gold, "\n");
    Assert.assertEquals(actual, expected);
  }
}
