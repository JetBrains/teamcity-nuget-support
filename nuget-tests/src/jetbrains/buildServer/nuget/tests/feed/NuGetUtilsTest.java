package jetbrains.buildServer.nuget.tests.feed;

import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetUtils;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.TestFor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Dmitry.Tretyakov
 *         Date: 29.08.2016
 *         Time: 16:38
 */
public class NuGetUtilsTest {
    @Test
    @TestFor(issues = "TW-43254")
    public void test_getRegularValue() {
        final Map<String, String> attributes = CollectionsUtil.asMap("Deps", "1");
        final String value = NuGetUtils.getValue(attributes, "Deps");
        Assert.assertEquals(value, "1");
    }

    @Test
    @TestFor(issues = "TW-43254")
    public void test_getCombinedValue() {
        final Map<String, String> attributes = CollectionsUtil.asMap(
                "Deps", "1",
                "Deps1", "2",
                "Deps2", "3",
                "Deps5", "5");
        final String value = NuGetUtils.getValue(attributes, "Deps");
        Assert.assertEquals(value, "123");
    }
}
