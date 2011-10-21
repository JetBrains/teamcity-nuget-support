/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.integration.feed;


import jetbrains.buildServer.NetworkUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Petrenko
 *         Created: 27.07.2009 16:25:49
 */
public abstract class SimpleHttpServerBase {
  public static final String STATUS_LINE_200 = "HTTP/1.0 200 Ok";
  public static final String STATUS_LINE_500 = "HTTP/1.0 500 Error";
  public static final String STATUS_LINE_404 = "HTTP/1.0 404 Not Found";

  private volatile boolean myStopped;

  private ServerSocket mySocket;
  private int myPort;
  private int myWaitAfterAccept = -1;
  private final Semaphore myWaitAfterAcceptHandle = new Semaphore(1){{acquireUninterruptibly();}};
  private Thread myProcessingThread;
  protected Pattern requestParser = Pattern.compile(".*GET\\s+(\\S+)\\s.*", Pattern.DOTALL);

  public SimpleHttpServerBase() {
    myPort = NetworkUtil.getFreePort(1025);
  }

  protected static Response getFileResponse(@NotNull final File file, @NotNull List<String> headers) {
    List<String> fileHeaders = new ArrayList<String>();
    fileHeaders.addAll(headers);
    Date lastModified = new Date(file.lastModified());
    SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    fileHeaders.add("Last-Modfied: " + format.format(lastModified));
    fileHeaders.add("ETag: " + file.hashCode());

    FileInputStream content = null;
    try {
      content = new FileInputStream(file);
      return getFileResponse(content, fileHeaders);
    } catch (FileNotFoundException e) {
      return createStringResponse(STATUS_LINE_404, fileHeaders, "");
    } finally {
      FileUtil.close(content);
    }
  }

  protected static Response getFileResponse(@NotNull final InputStream content, @NotNull List<String> headers) {
    try {
      return createStreamResponse(STATUS_LINE_200, headers, content);
    } finally {
      FileUtil.close(content);
    }
  }

  public int getPort() {
    return myPort;
  }

  public void start() throws IOException {
    myStopped = false;

    createSocket();

    myProcessingThread = new Thread(new Runnable() {
      public void run() {
        try {
          while (!myStopped) {
            Socket connection = mySocket.accept();
            if (myWaitAfterAccept > 0) {
              try {
                if (myWaitAfterAcceptHandle.tryAcquire(myWaitAfterAccept, TimeUnit.SECONDS)) {
                  myWaitAfterAcceptHandle.release();
                }
              } catch (InterruptedException e) {
                //
              }
            }
            try {
              final InputStream is = connection.getInputStream();
              final OutputStream os = connection.getOutputStream();

              PrintStream ps = new PrintStream(os);
              StringBuilder sb = new StringBuilder();
              while (true) {
                final int c = is.read();
                if (c == -1) break;
                //System.out.print((char)c);
                sb.append((char)c);

                if (sb.length() > 4) {
                  final String last4 = sb.substring(sb.length() - 4);
                  if ("\r\n\r\n".equals(last4)) {
                    break;
                  }
                }
              }

              Response response;
              try {
                response = SimpleHttpServerBase.this.getResponse(sb.toString());
              } catch(Throwable t) {
                Loggers.TEST.error("Failed to get response. " + t.getMessage(), t);
                response = createStringResponse(STATUS_LINE_500, Collections.<String>emptyList(), "");
              }
              writeResponse(ps, response);

              ps.close();
            } catch (IOException e) {
              //
            } finally {
              connection.close();
            }
          }
        } catch (IOException e) {
          if (!myStopped) {
            e.printStackTrace();
          }
        }
      }
    }, "simple server");
    myProcessingThread.start();
  }

  private void writeResponse(final PrintStream ps, final Response response) throws IOException {
    ps.println(response.getStatusLine());
    for (String h : response.getHeaders()) {
      ps.println(h);
    }
    if (response.getLength() != null) {
      ps.print("Content-Length: " + response.getLength() + "\r\n");
    }
    //close headers section
    ps.print("\r\n\r\n");
    ps.println();
    ps.flush();
    response.printContent(ps);

    ps.flush();
  }

  private void createSocket() throws IOException {
    while (true) {
      try {
        mySocket = new ServerSocket(myPort, 50, null);
        break;
      } catch (IOException e) {
        myPort++;
      }
    }
    mySocket.setSoTimeout(32 * 4096);
  }

  public void stop() {
    if (myStopped) return;
    myWaitAfterAcceptHandle.release();

    myStopped = true;
    try {
      mySocket.close();
      mySocket = null;
    } catch (IOException e) {
      //
    }

    try {
      myProcessingThread.join(60*1000);
    } catch (InterruptedException e) {
      //
    }
  }

  public void setWaitAfterAccept(int seconds) {
    myWaitAfterAccept = seconds;
  }

  protected abstract Response getResponse(String httpHeader);

  @Nullable
  protected String getRequestPath(@NotNull final String request) {
    Matcher matcher = requestParser.matcher(request);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
  }

  public abstract static class Response {
    private final String myStatusLine;
    private final List<String> myHeaders;

    public Response(final String statusLine, final List<String> headers) {
      myStatusLine = statusLine;
      myHeaders = headers;
    }

    public String getStatusLine() {
      return myStatusLine;
    }

    public List<String> getHeaders() {
      return myHeaders;
    }

    public abstract void printContent(PrintStream ps) throws IOException;

    public abstract Integer getLength();
  }


  public static Response createStringResponse(final String statusLine,
                                              final List<String> headers,
                                              final String content) {
    return new Response(statusLine, headers) {
      @Override
      public void printContent(PrintStream ps) {
        ps.print(content);
      }

      @Override
      public Integer getLength() {
        return content.length();
      }
    };
  }

  public static Response createStreamResponse(final String statusLine,
                                              final List<String> headers,
                                              final byte[] content) {
    return new Response(statusLine, headers) {
      @Override
      public void printContent(PrintStream ps) throws IOException {
        ps.write(content);
      }

      @Override
      public Integer getLength() {
        return content.length;
      }
    };
  }

  public static Response createStreamResponse(final String statusLine,
                                              final List<String> headers,
                                              final InputStream content) {
    return new Response(statusLine, headers) {
      @Override
      public void printContent(PrintStream ps) throws IOException {
        int data;
        while ((data = content.read()) != -1) {
          ps.write(data);
        }
      }

      @Override
      public Integer getLength() {
        return null;
      }
    };
  }

}
