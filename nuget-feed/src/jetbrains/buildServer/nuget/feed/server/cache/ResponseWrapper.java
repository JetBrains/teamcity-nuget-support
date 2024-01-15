

package jetbrains.buildServer.nuget.feed.server.cache;

import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.04.13 9:44
 */
public class ResponseWrapper extends HttpServletResponseWrapper {
  private final ByteArrayOutputStream myCache = new ByteArrayOutputStream(65536);
  private final Map<String, String> myHeaders = new HashMap<String, String>();
  private final ResponseOutputStream myOutput;
  private final PrintWriter myWriter;
  private int myStatus = SC_OK;

  public ResponseWrapper(@NotNull final HttpServletResponse response) throws IOException {
    super(response);
    myOutput = new ResponseOutputStream(myCache);
    myWriter = new PrintWriter(new OutputStreamWriter(myOutput, "utf-8"), false);
  }

  @Override
  public void setHeader(String name, String value) {
    myHeaders.put(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    myHeaders.put(name, value);
  }

  @Override
  public void setStatus(int sc) {
    myStatus = sc;
  }

  @Override
  public void setContentType(String type) {
  }

  @Override
  public void setContentLength(int len) {
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return myOutput;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return myWriter;
  }

  public void closeAll() throws IOException {
    myWriter.close();
    myOutput.close();
  }

  @NotNull
  public ResponseCacheEntry build() throws IOException {
    closeAll();
    return new ResponseCacheEntry(myHeaders, myCache.toByteArray(), myStatus);
  }
}
