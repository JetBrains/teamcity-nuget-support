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

<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="name" scope="request" type="java.lang.String"/>
<jsp:useBean id="clazz" scope="request" type="java.lang.String"/>
<jsp:useBean id="nugetExeDefaultSpecified" scope="request" type="java.lang.Boolean"/>

<c:choose>
  <c:when test="${nugetExeDefaultSpecified}">
    <l:settingsGroup title="NuGet settings" className="advancedSetting">
      <tr class="advancedSetting">
        <th>NuGet.exe<l:star/>:</th>
        <td>
          <jsp:include page="../tool/runnerSettings.html?name=${name}&class=${clazz}"/>
        </td>
      </tr>
    </l:settingsGroup>
  </c:when>
  <c:otherwise>
    <l:settingsGroup title="NuGet settings">
      <tr>
        <th>NuGet.exe<l:star/>:</th>
        <td>
          <jsp:include page="../tool/runnerSettings.html?name=${name}&class=${clazz}"/>
        </td>
      </tr>
    </l:settingsGroup>
  </c:otherwise>
</c:choose>