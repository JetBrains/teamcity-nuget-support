/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.util.StringUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * @author Dmitry.Tretyakov
 *         Date: 05.08.2016
 *         Time: 16:13
 */
public class RequestWrapper implements HttpServletRequest {
  private final String myQueryString;
  private final String myPath;
  private final String myServletPath;
  private final Map<String, Object> myAttributes = new HashMap<>();
  private final Map<String, String> myHeaders = new HashMap<>();
  private String myMethod = "GET";
  private int myServerPort = -1;
  private byte[] myBody;

  public RequestWrapper(final String servletPath, String request) {
    final String path = request.length() > servletPath.length() ? request.substring(servletPath.length() + 1) : "";
    final int queryStringIndex = path.indexOf("?");
    myServletPath = servletPath;
    myQueryString = queryStringIndex < 0 ? StringUtil.EMPTY : path.substring(queryStringIndex + 1);
    myPath = queryStringIndex < 0 ? path : path.substring(0, queryStringIndex);
  }

  @Override
  public String getAuthType() {
    return null;
  }

  @Override
  public Cookie[] getCookies() {
    return new Cookie[0];
  }

  @Override
  public long getDateHeader(String s) {
    return 0;
  }

  @Override
  public String getHeader(String name) {
    return myHeaders.get(name);
  }

  public void setHeader(String header, String value) {
    myHeaders.put(header, value);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    final Vector<String> values = new Vector<>();
    final String header = getHeader(name);
    if (header != null) {
      values.add(header);
    }
    return values.elements();
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return new Vector<>(myHeaders.keySet()).elements();
  }

  @Override
  public int getIntHeader(String s) {
    return 0;
  }

  @Override
  public String getMethod() {
    return myMethod;
  }

  public void setMethod(String method) {
    myMethod = method;
  }

  @Override
  public String getPathInfo() {
    return "/" + myPath;
  }

  @Override
  public String getPathTranslated() {
    return null;
  }

  @Override
  public String getContextPath() {
    return StringUtil.EMPTY;
  }

  @Override
  public String getQueryString() {
    return myQueryString;
  }

  @Override
  public String getRemoteUser() {
    return null;
  }

  @Override
  public boolean isUserInRole(String s) {
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    return null;
  }

  @Override
  public String getRequestURI() {
    return myServletPath + "/" + myPath;
  }

  @Override
  public StringBuffer getRequestURL() {
    return new StringBuffer("http://localhost" + myServletPath + "/" + myPath);
  }

  @Override
  public String getServletPath() {
    return myServletPath;
  }

  @Override
  public HttpSession getSession(boolean b) {
    return null;
  }

  @Override
  public HttpSession getSession() {
    return null;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
    return false;
  }

  @Override
  public void login(String s, String s1) throws ServletException {

  }

  @Override
  public void logout() throws ServletException {

  }

  @Override
  public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
    return null;
  }

  @Override
  public Part getPart(String s) throws IOException, IllegalStateException, ServletException {
    return null;
  }

  @Override
  public Object getAttribute(String s) {
    return myAttributes.get(s);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return new Vector<>(myAttributes.keySet()).elements();
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

  }

  @Override
  public int getContentLength() {
    return myBody.length;
  }

  @Override
  public String getContentType() {
    return myHeaders.get("Content-Type");
  }

  public void setContentType(String contentType) {
    myHeaders.put("Content-Type", contentType);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    final int[] position = {0};
    return new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return position[0] < myBody.length ? myBody[position[0]++] : -1;
      }
    };
  }

  public void setBody(final byte[] body) {
    myBody = body;
  }

  @Override
  public String getParameter(String s) {
    return null;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return null;
  }

  @Override
  public String[] getParameterValues(String s) {
    return new String[0];
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return null;
  }

  @Override
  public String getProtocol() {
    return null;
  }

  @Override
  public String getScheme() {
    return "http";
  }

  @Override
  public String getServerName() {
    return "localhost";
  }

  @Override
  public int getServerPort() {
    return myServerPort;
  }

  public void setServerPort(final int port) {
    myServerPort = port;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return null;
  }

  @Override
  public String getRemoteAddr() {
    return null;
  }

  @Override
  public String getRemoteHost() {
    return null;
  }

  @Override
  public void setAttribute(String s, Object o) {
    myAttributes.put(s, o);
  }

  @Override
  public void removeAttribute(String s) {
    myAttributes.remove(s);
  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return null;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  @Override
  public String getRealPath(String s) {
    return null;
  }

  @Override
  public int getRemotePort() {
    return 0;
  }

  @Override
  public String getLocalName() {
    return null;
  }

  @Override
  public String getLocalAddr() {
    return null;
  }

  @Override
  public int getLocalPort() {
    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public AsyncContext startAsync() {
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    return null;
  }
}