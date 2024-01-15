

package jetbrains.buildServer.nuget.tests.integration;

import java.util.ArrayList;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.util.HttpAuthServer;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Nullable;

/**
 * Created 02.01.13 15:36
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class MockNuGetAuthHTTP {
  private NuGetHttpServer myHttp;
  private final NuGetAPIVersion myApiVersion;

  public MockNuGetAuthHTTP() {
    this(NuGetAPIVersion.V2);
  }

  public MockNuGetAuthHTTP(NuGetAPIVersion apiVersion) {
    myApiVersion = apiVersion;
  }

  @NotNull
  public String getSourceUrl() {
    return myHttp.getSourceUrl();
  }

  @NotNull
  public String getPackageId() {
    return "FineCollection";
  }

  @NotNull
  public String getDownloadUrl() {
    checkServerIsRunning();
    return myHttp.getDownloadUrl();
  }

  @NotNull
  public NuGetFeedCredentials getCredentials() {
    return new NuGetFeedCredentials(getUsername(),  getPassword());
  }

  @NotNull
  public String getUsername() {
    checkServerIsRunning();
    return myHttp.getUsername();
  }

  @NotNull
  public String getPassword() {
    checkServerIsRunning();
    return myHttp != null ? myHttp.getPassword() : "";
  }

  public boolean getIsAuthorized(){
    checkServerIsRunning();
    return myHttp.isAuthorized();
  }

  public void stop() {
    if (myHttp != null) {
      myHttp.stop();
      myHttp = null;
    }
  }

  public void start() throws IOException {
    myHttp = new NuGetHttpServer();

    int port = getServerPort();
    setServerPort(myHttp, port);

    myHttp.start();
  }

  public MockNuGetAuthHTTP withPackage(final String packageName, final String version, final String metadataFilePath, final String nupkgDataPath, final boolean isLatestVersion) {
    myHttp.withPackage(packageName, version, metadataFilePath, nupkgDataPath, isLatestVersion);
    return this;
  }

  private void checkServerIsRunning() throws Error {
    if (myHttp == null) {
      throw new Error("NuGet mock http server has not been started");
    }
  }

  private void setServerPort(HttpAuthServer server, int port) {
    try {
      Field field = SimpleHttpServerBase.class.getDeclaredField("myPort");
      field.setAccessible(true);
      field.set(server, port);
    } catch (Exception ignored) {
    }
  }

  private static int getServerPort() throws IOException {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(0);
      return socket.getLocalPort();
    } finally {
      if (socket != null) {
        socket.close();
      }
    }
  }

  private void log(String message) {
    System.out.println("[mock feed] " + message);
  }

  public static interface Action {
    void runTest(@NotNull MockNuGetAuthHTTP http) throws Throwable;
  }

  public static void executeTest(@NotNull Action action) throws Throwable {
    MockNuGetAuthHTTP http = new MockNuGetAuthHTTP();
    http.start();
    try {
      action.runTest(http);
    } finally {
      http.stop();
    }
  }

  private class NuGetHttpServer extends HttpAuthServer implements MockNuGetHTTPServerApi {
    private String mySourceUrl;
    private String myDownloadUrl;
    private String myUsername;
    private String myPassword;
    private final AtomicBoolean myIsAuthorized = new AtomicBoolean(false);

    private final MockNuGetRequestHandler myHandler = new MockNuGetRequestHandler(this);

    @Override
    public NuGetAPIVersion getApiVersion() {
      return myApiVersion;
    }

    @Nullable
    @Override
    public String getRequestPath(@NotNull final String request) {
      return super.getRequestPath(request);
    }

    @Override
    public String getSourceUrl() {
      return mySourceUrl;
    }

    @Override
    public String getDownloadUrl() {
      return myDownloadUrl;
    }

    public String getUsername() {
      return myUsername;
    }

    public String getPassword() {
      return myPassword;
    }

    public boolean isAuthorized() {
      return myIsAuthorized.get();
    }

    @Override
    public void start() throws IOException {
      super.start();

      myUsername = "u-" + StringUtil.generateUniqueHash();
      myPassword = "p-" + StringUtil.generateUniqueHash();
      myIsAuthorized.set(false);

      mySourceUrl = "http://localhost:" + getPort() + "/nuget/";
      myDownloadUrl = "http://localhost:" + getPort() + "/download/";
    }

    public Response createFileResponse(@NotNull final File testDataPath, @NotNull List<String> asList) {
      return getFileResponse(testDataPath, asList);
    }

    public void withPackage(final String packageName, final String version, final String metadataFilePath, final String nupkgDataPath, final boolean isLatestVersion) {
      myHandler.withPackage(packageName, version, metadataFilePath, nupkgDataPath, isLatestVersion);
    }

    @Override
    protected Response getAuthorizedResponse(final String request) throws IOException {
      try {
        return myHandler.getResponse(request);
      } catch (JDOMException e) {
        e.printStackTrace();
        return createStringResponse(STATUS_LINE_500, new ArrayList<String>(), e.getMessage());
      }
    }

    @NotNull
    @Override
    protected Response getNotAuthorizedResponse(final String request) {
      log("Not authorized: " + request);
      return super.getNotAuthorizedResponse(request);
    }

    @Override
    protected boolean authorizeUser(@NotNull String loginPassword) {
      if ((myUsername + ":" + myPassword).equals(loginPassword)) {
        myIsAuthorized.set(true);
        log("Authorized user with password: " + loginPassword);
        return true;
      }
      log("Can't authorize user. Password is incorrect.");
      return false;
    }
  }
}
