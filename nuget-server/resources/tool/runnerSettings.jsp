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
<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="name" scope="request" type="java.lang.String"/>
<jsp:useBean id="clazz" scope="request" type="java.lang.String"/>
<jsp:useBean id="style" scope="request" type="java.lang.String"/>
<jsp:useBean id="value" scope="request" type="java.lang.String"/>
<jsp:useBean id="settingsUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="items" scope="request" type="java.util.Collection<jetbrains.buildServer.nuget.server.toolRegistry.ui.ToolInfo >"/>

<props:selectProperty name="${name}" className="${clazz}" style="${style}">
  <c:set var="isSelected" value="${value eq ''}"/>
  <props:option value="" selected="${isSelected}">-- Select NuGet version to run --</props:option>
  <c:set var="hasSelected" value="${isSelected}"/>
  <c:forEach var="it" items="${items}">
    <c:set var="isSelected" value="${it.id eq value}"/>
    <props:option value="${it.id}" selected="${isSelected}"><c:out value="${it.version}"/></props:option>
    <c:if test="${isSelected}"><c:set var="hasSelected" value="${true}"/></c:if>
  </c:forEach>
  <c:if test="${not hasSelected}">
    <props:option value="${value}" selected="${true}">Custom: <c:out value="${value}"/></props:option>
  </c:if>
</props:selectProperty>
<span class="smallNote">Specify NuGet.exe version.
  Check intalled NuGet Commandline tools in <a href="<c:url value="${settingsUrl}"/>">NuGet Settings</a>
</span>
<span class="error" id="error_${name}"></span>
