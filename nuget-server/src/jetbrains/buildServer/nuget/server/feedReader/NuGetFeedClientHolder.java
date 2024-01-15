

package jetbrains.buildServer.nuget.server.feedReader;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedHttpClientHolder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Created by Evgeniy.Koshkin on 11-Mar-16.
 */
public class NuGetFeedClientHolder implements NuGetFeedClient {
  private final NuGetFeedHttpClientHolder myFeedClient = new NuGetFeedHttpClientHolder();

  @NotNull
  @Override
  public HttpResponse execute(@NotNull HttpUriRequest httpUriRequest) throws IOException {
    return myFeedClient.execute(httpUriRequest);
  }

  @NotNull
  @Override
  public NuGetFeedClient withCredentials(@Nullable NuGetFeedCredentials nuGetFeedCredentials) {
    return myFeedClient.withCredentials(nuGetFeedCredentials);
  }

  @Override
  public boolean hasCredentials() {
    return myFeedClient.hasCredentials();
  }

  public void dispose() {
    myFeedClient.dispose();
  }
}
