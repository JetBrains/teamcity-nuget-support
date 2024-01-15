<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


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
