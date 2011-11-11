<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

<jsp:useBean id="nugetPackages" scope="request" type="java.util.Map<java.lang.String, java.lang.String>"/>
<c:set var="numberOfPackages" value="${fn:length(nugetPackages)}" />

This build downloaded ${numberOfPackages} NuGet package<bs:s val="${numberOfPackages}"/>.

<c:choose>
  <c:when test="${fn:length(nugetPackages) eq 0}">
    No packages were reported
  </c:when>
  <c:otherwise>
    <table class="runnerFormTable" style="width:40em">
      <thead>
      <tr>
        <th>Package Name</th>
        <th>Package Version</th>
      </tr>
      </thead>
      <c:forEach var="it" items="${nugetPackages}">
        <tr>
          <td><c:out value="${it.key}"/></td>
          <td><c:out value="${it.value}"/></td>
        </tr>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>

