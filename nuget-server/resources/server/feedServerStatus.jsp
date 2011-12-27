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

<jsp:useBean id="serverUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="nugetStatusRefreshUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="imagesBase" scope="request" type="java.lang.String" />
<jsp:useBean id="serverStatus" scope="request" type="jetbrains.buildServer.nuget.server.feed.server.NuGetServerStatus" />
<jsp:useBean id="privateFeedUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="publicFeedUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="isGuestEnabled" type="java.lang.Boolean" scope="request"/>

<c:set var="nugetStatusRefreshFullUrl"><c:url value="${nugetStatusRefreshUrl}"/></c:set>

<h2 class="noBorder" style="padding-top:2em; padding-bottom: 1em;">NuGet Server status:</h2>

<c:set var="isError" value="${false}"/>

<bs:refreshable containerId="nugetServerStatus" pageUrl="${nugetStatusRefreshFullUrl}">
  <c:choose>
    <c:when test="${serverStatus.scheduledToStart}">
      <div style="">
        <img src="<c:url value='${imagesBase}/restarting.gif'/>" alt="starting"/>
        Server is starting

        <span class="smallNote" style="margin-left:1em">NuGet Feed server is not runnig now and will be started soon.</span>
      </div>
    </c:when>

    <c:when test="${not serverStatus.running}">
      <div style="">
        <img src="<c:url value='${imagesBase}/stopped.gif'/>" alt="stopped"/>
        Server is stopped
      </div>
    </c:when>

    <%-- server is runnning --%>
    <c:when test="${empty serverStatus.serverAccessible}">
      <div style="">
        <img src="<c:url value='${imagesBase}/starting.gif'/>" alt="starting"/>
        Server is starting
      </div>
    </c:when>
    <c:when test="${not serverStatus.serverAccessible}">
      <c:set var="isError" value="${true}"/>
      <div style="">
        <img src="<c:url value='${imagesBase}/error.gif'/>" alt="error"/>
        Ping Failed
        <span class="smallNote" style="margin-left:1em">Check TeamCity Server Url is accessible from localhost. NuGet server will be restarted</span>
      </div>
    </c:when>
    <c:when test="${serverStatus.serverAccessible}">
      <div style="">
        <img src="<c:url value='${imagesBase}/running.gif'/>" alt="starting"/>
        Running
        <span class="smallNote" style="margin-left:1em">NuGet Feed server is running now.</span>
      </div>
    </c:when>
  </c:choose>

  <c:if test="${isError}">
    <p>
    See <a href="<c:url value='/admin/admin.html?item=diagnostic&tab=log4j&file=teamcity-nuget-server.log'/>">NuGet Server logs</a> for more details
    </p>
  </c:if>



    <h2 class="noBorder" style="padding-top: 2em;">NuGet Server Url:</h2>
    <table class="runnerFormTable nugetUrls">
      <tr>
        <th>Authenticated Feed Url:</th>
        <td>
          <c:set var="url"><c:url value="${serverUrl}${privateFeedUrl}"/></c:set>
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
            <span class="smallNote">Guest user is disabled. You need to enable guest user login in TeamCity general server settings for public feed to work.</span>
          </c:when>
          <c:otherwise>
            <c:set var="url"><c:url value="${serverUrl}${publicFeedUrl}"/></c:set>
            <div><a href="${url}">${url}</a></div>
            <span class="smallNote">No authentication is required.</span>
          </c:otherwise>
        </c:choose>
        </td>
      </tr>
    </table>

</bs:refreshable>
