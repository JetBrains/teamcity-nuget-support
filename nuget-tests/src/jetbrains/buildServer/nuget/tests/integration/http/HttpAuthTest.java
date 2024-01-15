

package jetbrains.buildServer.nuget.tests.integration.http;

import jetbrains.buildServer.util.HttpAuthServer;

import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 16:30
 */
public class HttpAuthTest {
  public static void main(String[] args) throws IOException {
    HttpAuthServer s = new HttpAuthServer();
    s.start();

    System.out.println("s.getPort() = " + s.getPort());

    System.in.read();
    s.stop();
  }
}
