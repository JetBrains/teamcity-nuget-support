

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.checker.PackageCheckerNuGetBulk;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 19:49
 */
public class PackageCheckerNuGetBulkTest extends PackageCheckerTestBase<PackageCheckerNuGetBulk> {
  @Override
  protected PackageCheckerNuGetBulk createChecker() {
    return new PackageCheckerNuGetBulk(myCommand, mySettings);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m.checking(new Expectations(){{
      allowing(mySettings).getMaxPackagesToQueryInBulk(); will(returnValue(11));
    }});
  }

  @Test
  public void test_available_01() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).allowBulkMode();
      will(returnValue(false));
    }});
    Assert.assertFalse(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));
  }

  @Test
  public void test_available_01x() throws IOException {
    Assert.assertFalse(myChecker.accept(new PackageCheckRequest(javaMode(), ref())));
  }

  @Test
  public void test_available_02() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).allowBulkMode();
      will(returnValue(true));
    }});
    Assert.assertTrue(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));
  }

  @Test
  public void test_bulk() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();


    final CheckablePackage task = checkablePackage("qq", ref);
    m.checking(new Expectations(){{
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(returnValue(Collections.emptyMap()));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_success() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final ListPackagesResult aaa = fromCollection(ref.toInfo("aaa"));
    final Map<SourcePackageReference, ListPackagesResult> result = Collections.singletonMap(ref, aaa);
    final CheckablePackage task = checkablePackage("aqqq", ref, equal(CheckResult.fromResult(aaa)));
    m.checking(new Expectations(){{
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(returnValue(result));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));
    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_errorMessage() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final ListPackagesResult aaa = fromError("something failed");
    final Map<SourcePackageReference, ListPackagesResult> result = Collections.singletonMap(ref, aaa);
    final CheckablePackage task = checkablePackage("aqqq", ref, equal(CheckResult.failed("something failed")));

    m.checking(new Expectations(){{
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(returnValue(result));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));
    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_empty_result_failure() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final ListPackagesResult aaa = fromCollection();
    final Map<SourcePackageReference, ListPackagesResult> result = Collections.singletonMap(ref, aaa);
    final CheckablePackage task = checkablePackage("aqqq", ref, empty());
    m.checking(new Expectations(){{
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(returnValue(result));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));
    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_limit() throws NuGetExecutionException {
    final int N = 156;
    final List<CheckablePackage> tasks = new ArrayList<CheckablePackage>();
    for(int i = 0; i < N; i++) {
      tasks.add(checkablePackage("task-" + i, new SourcePackageReference("a", "package-" + i, null)));
    }

    final Map<SourcePackageReference, ListPackagesResult> result = Collections.emptyMap();

    m.checking(new Expectations(){{
      final int bunch = mySettings.getMaxPackagesToQueryInBulk();
      between(N/bunch, N/bunch+1).of(myCommand).checkForChanges(with(any(File.class)), with(sz(SourcePackageReference.class, bunch))); will(returnValue(result));
    }});

    myChecker.update(myExecutor, tasks);

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_limit_chunks() throws NuGetExecutionException {
    final SourcePackageReference p1 = new SourcePackageReference("a", "package-0", null);
    final SourcePackageReference p2 = new SourcePackageReference("a", "package-0", "[42]");
    final Map<SourcePackageReference, ListPackagesResult> result = Collections.emptyMap();

    m.checking(new Expectations(){{
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(p1))); will(returnValue(result));
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(p2))); will(returnValue(result));
    }});

    final List<CheckablePackage> tasks = new ArrayList<CheckablePackage>();
    tasks.add(checkablePackage("task-0", p1));
    tasks.add(checkablePackage("task-1", p2));
    myChecker.update(myExecutor, tasks);

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_error() throws NuGetExecutionException {
    @SuppressWarnings({"unchecked"})
    final SourcePackageReference ref = ref();

    m.checking(new Expectations(){{
      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(throwException(new NuGetExecutionException("aaa")));
    }});

    final CheckablePackage task = checkablePackage("aaa", ref, failed("aaa"));
    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @NotNull
  private CheckablePackage checkablePackage(@NotNull final String name,
                                            @NotNull final SourcePackageReference ref) {
    return checkablePackage(name, ref, null);
  }

  @NotNull
  private CheckablePackage checkablePackage(@NotNull final String name,
                                            @NotNull final SourcePackageReference ref,
                                            @Nullable final Matcher<CheckResult> result) {
    final CheckablePackage cp = m.mock(CheckablePackage.class, name);
    m.checking(new Expectations(){{
      allowing(cp).getPackage(); will(returnValue(ref));
      allowing(cp).getMode(); will(returnValue(nugetMode()));

      oneOf(cp).setExecuting();
      if (result == null) {
        oneOf(cp).setResult(with(any(CheckResult.class)));
      } else {
        oneOf(cp).setResult(with(result));
      }
    }});
    return cp;
  }
}
