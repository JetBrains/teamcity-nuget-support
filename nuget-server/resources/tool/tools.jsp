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
<jsp:useBean id="tools" type="jetbrains.buildServer.nuget.server.toolRegistry.tab.ToolsModel" scope="request"/>

<h3>Installed NuGet Versions</h3>
<c:choose>
  <c:when test="${fn:length(tools.installed) eq 0}">
    No NuGet packages installed
  </c:when>
  <c:otherwise>
    <c:forEach var="tool" items="${tools.installed}">
      <div>
        NuGet version: <c:out value="${tool.version}"/>
      </div>
    </c:forEach>
  </c:otherwise>
</c:choose>
<div class="addNew"><a href="#">Download NuGet</a></div>
<div class="addNew"><a href="#">Install custom NuGet.exe</a></div>


<%--
<h3>Available NuGet Versions</h3>
<c:choose>
  <c:when test="${tools.toolsToInstallComputed}">
    <c:forEach var="tool" items="${tools.toolsToInstall}">
      <div>
        NuGet version: <c:out value="${tool.version}"/> <a href="#" class="addNew">Install</a>
      </div>
    </c:forEach>
  </c:when>
  <c:otherwise>

  </c:otherwise>
</c:choose>


--%>
