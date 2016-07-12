<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  ~ Copyright 2000-2014 JetBrains s.r.o.
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

<jsp:useBean id="packages" scope="request" type="jetbrains.buildServer.nuget.common.PackageDependencies"/>
<jsp:useBean id="feedPackages" scope="request" type="java.util.Set<jetbrains.buildServer.nuget.common.NuGetPackageInfo>"/>

<h3>Used Packages</h3>
<c:set var="numberOfUsedPackages" value="${fn:length(packages.usedPackages)}"/>
This build downloaded and used ${numberOfUsedPackages} NuGet package<bs:s val="${numberOfUsedPackages}"/>.
<c:if test="${numberOfUsedPackages gt 0}">
  <table class="settings" style="width:50em">
    <thead>
    <tr>
      <th class="name">Package Name</th>
      <th class="value">Package Version</th>
    </tr>
    </thead>
    <c:forEach var="it" items="${packages.usedPackages}">
      <tr>
        <td class="name"><c:out value="${it.id}"/></td>
        <td class="value"><c:out value="${it.version}"/></td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:set var="numberOfCreatedPackages" value="${fn:length(packages.createdPackages)}" />
<h3>Created Packages</h3>
This build created ${numberOfCreatedPackages} NuGet package<bs:s val="${numberOfCreatedPackages}"/>.
<c:if test="${numberOfCreatedPackages gt 0}">
  <table class="settings" style="width:50em">
    <thead>
    <tr>
      <th class="name">Package Name</th>
      <th class="value">Package Version</th>
    </tr>
    </thead>
    <c:forEach var="it" items="${packages.createdPackages}">
      <tr>
        <td class="name"><c:out value="${it.id}"/></td>
        <td class="value"><c:out value="${it.version}"/></td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:set var="numberOfPublishedPackages" value="${fn:length(feedPackages)}" />
<c:if test="${numberOfPublishedPackages gt 0}">
  <h3>Published to TeamCity NuGet feed</h3>
  TeamCity detected and added ${numberOfPublishedPackages} NuGet package<bs:s val="${numberOfPublishedPackages}"/> to the feed.
  <table class="settings" style="width:50em">
    <thead>
    <tr>
      <th class="name">Package Name</th>
      <th class="value">Package Version</th>
    </tr>
    </thead>
    <c:forEach var="it" items="${feedPackages}">
      <tr>
        <td class="name"><c:out value="${it.id}"/></td>
        <td class="value"><c:out value="${it.version}"/></td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<c:set var="numberOfPublishedPackages" value="${fn:length(packages.publishedPackages)}" />
<c:if test="${numberOfPublishedPackages gt 0}">
  <h3>Published to External NuGet Feeds</h3>
  TeamCity detected and added ${numberOfPublishedPackages} NuGet package<bs:s val="${numberOfPublishedPackages}"/> to the feed.
  <table class="settings" style="width:50em">
    <thead>
    <tr>
      <th class="name">Package Name</th>
      <th class="value">Package Version</th>
      <th class="value">Package Source</th>
    </tr>
    </thead>
    <c:forEach var="it" items="${packages.publishedPackages}">
      <tr>
        <td class="name"><c:out value="${it.packageInfo.id}"/></td>
        <td class="value"><c:out value="${it.packageInfo.version}"/></td>
        <td class="value">
          <c:choose>
            <c:when test="${not empty it.source}"><c:out value="${it.source}"/></c:when>
            <c:otherwise><em>default</em></c:otherwise>
          </c:choose>
      </tr>
    </c:forEach>
  </table>
</c:if>

