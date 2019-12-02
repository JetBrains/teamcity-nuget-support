package jetbrains.buildServer.nuget.tests.integration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import jetbrains.buildServer.util.SimpleThreadedHttpServer;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockNuGetHTTP {
  private NuGetHttpServer myHttp;
  private final NuGetAPIVersion myApiVersion;

  public MockNuGetHTTP() {
    this(NuGetAPIVersion.V2);
  }

  public MockNuGetHTTP(NuGetAPIVersion apiVersion) {
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

  private void checkServerIsRunning() throws Error {
    if (myHttp == null) {
      throw new Error("NuGet mock http server has not been started");
    }
  }

  private void setServerPort(SimpleThreadedHttpServer server, int port) {
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
    void runTest(@NotNull MockNuGetHTTP http) throws Throwable;
  }

  public static void executeTest(@NotNull Action action) throws Throwable {
    MockNuGetHTTP http = new MockNuGetHTTP();
    http.start();
    try {
      action.runTest(http);
    } finally {
      http.stop();
    }
  }

  private class NuGetHttpServer extends SimpleThreadedHttpServer implements MockNuGetHTTPServerApi {
    private String mySourceUrl;
    private String myDownloadUrl;

    private final MockNuGetRequestHandler handler = new MockNuGetRequestHandler(this);

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

    @Override
    public void start() throws IOException {
      super.start();

      mySourceUrl = "http://localhost:" + getPort() + "/nuget/";
      myDownloadUrl = "http://localhost:" + getPort() + "/download/";
    }

    public Response createFileResponse(@NotNull final File testDataPath, @NotNull List<String> asList) {
      return getFileResponse(testDataPath, asList);
    }

    @Override
    protected Response getResponse(final String request) {
      System.out.println(request);
      try {
        return handler.getResponse(request);
      } catch (IOException | JDOMException e) {
        e.printStackTrace();
        return createStringResponse(STATUS_LINE_500, new ArrayList<String>(), e.getMessage());
      }
    }
  }
}

