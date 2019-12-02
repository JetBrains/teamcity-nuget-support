package jetbrains.buildServer.nuget.tests.integration;

import java.io.File;
import java.util.List;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MockNuGetHTTPServerApi {
  NuGetAPIVersion getApiVersion();

  @Nullable
  String getRequestPath(@NotNull final String request);

  SimpleHttpServerBase.Response createFileResponse(@NotNull final File testDataPath, @NotNull List<String> asList);

  String getSourceUrl();

  String getDownloadUrl();
}
