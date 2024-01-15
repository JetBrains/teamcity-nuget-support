

package jetbrains.buildServer.nuget.server.feedReader;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedReader;
import jetbrains.buildServer.nuget.feedReader.NuGetPackage;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedGetMethodFactory;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedUrlResolver;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetPackagesFeedParser;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedReaderImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Evgeniy.Koshkin on 11-Mar-16.
 */
public class NuGetFeedReaderHolder implements NuGetFeedReader {
  private final NuGetFeedReader myFeedReader;

  public NuGetFeedReaderHolder() {
    NuGetFeedGetMethodFactory getMethodFactory = new NuGetFeedGetMethodFactory();
    myFeedReader = new NuGetFeedReaderImpl(new NuGetFeedUrlResolver(getMethodFactory), getMethodFactory, new NuGetPackagesFeedParser());
  }

  @NotNull
  @Override
  public Collection<NuGetPackage> queryPackageVersions(@NotNull NuGetFeedClient nuGetFeedClient, @NotNull String feedUrl, @NotNull String packageId) throws IOException {
    return myFeedReader.queryPackageVersions(nuGetFeedClient, feedUrl, packageId);
  }

  @Override
  public void downloadPackage(@NotNull NuGetFeedClient nuGetFeedClient, @NotNull String downloadUrl, @NotNull File destination) throws IOException {
    myFeedReader.downloadPackage(nuGetFeedClient, downloadUrl, destination);
  }
}
