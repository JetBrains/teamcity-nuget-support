

package jetbrains.buildServer.nuget.feed.server.cache;

import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.04.13 22:34
 */
public class ResponseCacheEntry {
  private final Map<String, String> myHeaders = new TreeMap<>();
  private final byte[] myContent;
  private final int myStatus;

  public ResponseCacheEntry(@NotNull final Map<String, String> headers,
                            @NotNull final byte[] content,
                            final int status) {
    myHeaders.putAll(headers);
    myContent = content;
    myStatus = status;
  }

  public void handleRequest(@NotNull final HttpServletRequest request,
                            @NotNull final HttpServletResponse response) throws Exception {
    for (Map.Entry<String, String> e : myHeaders.entrySet()) {
      response.setHeader(e.getKey(), e.getValue());
    }
    response.setHeader("Content-Encoding", "gzip");
    response.setStatus(myStatus);

    ServletOutputStream stream = null;
    try {
      stream = response.getOutputStream();
      stream.write(myContent);
      stream.flush();
    } finally {
      FileUtil.close(stream);
    }
  }
}
