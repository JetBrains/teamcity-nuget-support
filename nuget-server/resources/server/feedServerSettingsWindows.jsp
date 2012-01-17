<%--
  ~ Copyright 2000-2011 JetBrains s.r.o.
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

<jsp:useBean id="nugetStatusRefreshUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="nugetSettingsPostUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="serverEnabled" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="privateFeedUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="publicFeedUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="isGuestEnabled" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="actualServerUrl" scope="request" type="java.lang.String" />

<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.server.feed.server.tab.FeedServerContants"/>

<c:set var="nugetStatusRefreshFullUrl"><c:url value="${nugetStatusRefreshUrl}"/></c:set>
<c:set var="nugetSettingsPostFullUrl"><c:url value="${nugetSettingsPostUrl}"/></c:set>

<bs:refreshable containerId="nugetEnableDisable" pageUrl="${nugetStatusRefreshFullUrl}">
  <div style="padding-top:1em;" data-url="${nugetSettingsPostFullUrl}">
    NuGet Server<bs:help file="NuGet"/> is
    <c:choose>
      <c:when test="${serverEnabled}">
        <strong>enabled</strong> <input type="button" class="btn" value="Disable" onclick="return BS.NuGet.FeedServer.disableFeedServer(this);" />
      </c:when>
      <c:otherwise>
        <strong>disabled</strong> <input type="button" class="btn" value="Enable" onclick="return BS.NuGet.FeedServer.enableFeedServer(this);" />
      </c:otherwise>
    </c:choose>
    <span><%--used for loading icon--%></span>
  </div>

  <c:if test="${serverEnabled}">
    <table class="runnerFormTable nugetUrls">
      <tr>
        <th>Authenticated Feed Url:</th>
        <td>
          <c:set var="url"><c:url value="${actualServerUrl}${privateFeedUrl}"/></c:set>
          <div><a href="${url}">${url}</a></div>
          <span class="smallNote">Access to the url requires HTTP authentication</span>
        </td>
      </tr>
      <tr>
        <th>Public Feed Url:</th>
        <td>
        <c:choose>
          <c:when test="${not isGuestEnabled}">
            <div>Not available.</div>
            <span class="smallNote">
              Guest user is disabled.
            </span>
            <span class="smallNote">
              You need to enable guest user login in
              TeamCity <a href="<c:url value="/admin/admin.html?item=serverConfigGeneral"/>">Global Settings</a>
              for public feed to work.
            </span>
          </c:when>
          <c:otherwise>
            <c:set var="url"><c:url value="${actualServerUrl}${publicFeedUrl}"/></c:set>
            <div><a href="${url}">${url}</a></div>
            <span class="smallNote">No authentication is required.</span>
          </c:otherwise>
        </c:choose>
        </td>
      </tr>
    </table>
  </c:if>

</bs:refreshable>
