

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 13.01.12 20:25
 */
public class NuGetJavaFeedContentPerformanceTest extends NuGetJavaFeedIntegrationTestBase {

  private static <T> int count(Iterator<T> itz) {
    int c = 0;
    while(itz.hasNext()) {
      c++;
      itz.next();
    }
    return c;
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void test_list_no_query_5000(final NugetFeedLibrary library) throws IOException {
    setODataSerializer(library);
    do_test_list_packages(5000, 100, "");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void test_list_query_by_id_5000(final NugetFeedLibrary library) throws IOException {
    setODataSerializer(library);
    do_test_list_packages(5000, 0.4, "?$filter=Id%20eq%20'Foo'");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void test_list_query_search_5000(final NugetFeedLibrary library) throws IOException {
    setODataSerializer(library);
    do_test_list_packages(
            5000,
            2,
            "?$filter=(((Id%20ne%20null)%20and%20substringof('mm',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('mm',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20mm%20',tolower(Tags)))" +
                    "&$orderby=Id" +
                    "&$skip=0" +
                    "&$top=30");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void test_list_query_search_50000(final NugetFeedLibrary library) throws IOException {
    setODataSerializer(library);
    do_test_list_packages(
            50000,
            5,
            "?$filter=(((Id%20ne%20null)%20and%20substringof('mm',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('mm',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20mm%20',tolower(Tags)))" +
                    "&$orderby=Id" +
                    "&$skip=0" +
                    "&$top=30");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void test_list_isLatestVersion_50000(final NugetFeedLibrary library) throws IOException {
    setODataSerializer(library);
    do_test_list_packages(
            50000,
            2,
            "?$filter=IsLatestVersion" +
                    "&$orderby=Id" +
                    "&$skip=0" +
                    "&$top=30");
  }


  private void do_test_list_packages(int sz, double time, @NotNull final String query) throws IOException {
    NuGetIndexEntry base = addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"), true);
    for(int i = 1; i < sz; i ++) {
      addMockPackage(base, false);
    }

    Assert.assertEquals(myIndex.getAll().size(), sz);

    final AtomicReference<String> s = new AtomicReference<>();
    try {
      assertTime(time, "aaa", 5, () -> {
        String req = openRequest("Packages()" + query);
        s.set(req);
      });
    } finally {
      final String req = s.get();
      System.out.println("req.length() = " + req.length());
      System.out.println(req);
      System.out.println(XmlUtil.to_s(XmlUtil.from_s(req)));
    }
  }
}
