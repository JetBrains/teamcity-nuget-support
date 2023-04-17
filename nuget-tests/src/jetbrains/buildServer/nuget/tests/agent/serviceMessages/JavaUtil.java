package jetbrains.buildServer.nuget.tests.agent.serviceMessages;

import jetbrains.buildServer.nuget.agent.serviceMessages.NuGetPackageServiceFeedPublisher;
import jetbrains.buildServer.nuget.common.index.NuGetPackageData;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;

import java.util.Collection;

public final class JavaUtil {

  public static void addAllowing(Expectations param, @Nullable NuGetPackageServiceFeedPublisher myPackagePublisher) {
    param.allowing(myPackagePublisher).publishPackages(param.with(Expectations.any(Collection.class)));
  }
}
