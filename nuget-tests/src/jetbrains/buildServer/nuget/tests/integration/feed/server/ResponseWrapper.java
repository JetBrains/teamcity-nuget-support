

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Serializable http response.
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

  private StringWriter sw = new StringWriter();
  private String myRedirectLocation = null;

  public ResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(sw);
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setWriteListener(WriteListener listener) {
      }

      @Override
      public void write(int b) throws IOException {
        sw.write(b);
      }
    };
  }

  public String toString() {
    return sw.toString();
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    super.sendRedirect(location);
    myRedirectLocation = location;
  }

  @Override
  public String getHeader(String name) {
    if ("Location".equalsIgnoreCase(name)) {
      return myRedirectLocation;
    }
    return super.getHeader(name);
  }
}
