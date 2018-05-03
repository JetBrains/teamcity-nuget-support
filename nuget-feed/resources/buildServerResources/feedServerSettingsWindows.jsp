<%--
  ~ Copyright 2000-2016 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="serverEnabled" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="isGuestEnabled" type="java.lang.Boolean" scope="request"/>

<jsp:useBean id="nugetStatusRefreshUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="nugetSettingsPostUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="privateFeedUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="publicFeedUrl" scope="request" type="java.lang.String" />

<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.feed.server.tab.FeedServerContants"/>

<c:set var="nugetStatusRefreshFullUrl"><c:url value="${nugetStatusRefreshUrl}"/></c:set>
<c:set var="nugetSettingsPostFullUrl"><c:url value="${nugetSettingsPostUrl}"/></c:set>

<bs:refreshable containerId="nugetEnableDisable" pageUrl="${nugetStatusRefreshFullUrl}">
  <div data-url="${nugetSettingsPostFullUrl}">
    NuGet Server<bs:help file="NuGet"/> is
    <c:choose>
      <c:when test="${serverEnabled}">
        <strong>enabled</strong> <c:if test="${afn:permissionGrantedGlobally('CHANGE_SERVER_SETTINGS')}"><input type="button" class="btn btn_mini" value="Disable" onclick="return BS.NuGet.FeedServer.disableFeedServer(this);" /></c:if>
      </c:when>
      <c:otherwise>
        <strong>disabled</strong> <c:if test="${afn:permissionGrantedGlobally('CHANGE_SERVER_SETTINGS')}"><input type="button" class="btn btn_mini" value="Enable" onclick="return BS.NuGet.FeedServer.enableFeedServer(this);" /></c:if>
      </c:otherwise>
    </c:choose>
    <span><%--used for loading icon--%></span>
  </div>

  <c:if test="${serverEnabled}">
    <table class="runnerFormTable nugetSettings">
      <tr>
        <th>Authenticated Feed URL:</th>
        <td>
          <c:set var="url"><c:url value="${privateFeedUrl}"/></c:set>
          <div><a href="${url}">${url}</a></div>
          <span class="smallNote">Lists all packages available for the currently authenticated user (user should have view project permission). Uses HTTP BASIC authentication</span>
        </td>
      </tr>
      <tr>
        <th>Public Feed URL:</th>
        <td>
        <c:choose>
          <c:when test="${not isGuestEnabled}">
            <div>Not available.</div>
            <span class="smallNote">
              Enable the guest user <bs:help file="Guest+User"/> login in
              TeamCity <a href="<c:url value="/admin/admin.html?item=auth"/>">Authentication</a> settings
              for public feed to work.
            </span>
          </c:when>
          <c:otherwise>
            <c:set var="url"><c:url value="${publicFeedUrl}"/></c:set>
            <div><a href="${url}">${url}</a></div>
            <span class="smallNote">Lists all packages from builds available for the guest<bs:help file="Guest+User"/> user</span>
          </c:otherwise>
        </c:choose>
        </td>
      </tr>
    </table>
  </c:if>

</bs:refreshable>
