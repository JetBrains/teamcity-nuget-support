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
public class TestFeedRequestWrapper implements HttpServletRequest {
  private final String myQueryString;
  private final String myPath;
  private final String myServletPath;
  private final Map<String, Object> myAttributes = new HashMap<>();

  public TestFeedRequestWrapper(final String servletPath, String request) {
    request = request.startsWith("/") ? request.substring(1) : request;
    final int queryStringIndex = request.indexOf("?");
    myServletPath = servletPath;
    myQueryString = queryStringIndex < 0 ? StringUtil.EMPTY : request.substring(queryStringIndex + 1);
    myPath = queryStringIndex < 0 ? request : request.substring(0, queryStringIndex);
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
  public String getHeader(String s) {
    return null;
  }

  @Override
  public Enumeration<String> getHeaders(String s) {
    return null;
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.emptyEnumeration();
  }

  @Override
  public int getIntHeader(String s) {
    return 0;
  }

  @Override
  public String getMethod() {
    return "GET";
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
    return StringUtil.EMPTY;
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
    return 0;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return null;
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
    return -1;
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
