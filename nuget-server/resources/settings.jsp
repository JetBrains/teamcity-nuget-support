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

<%@ include file="/include-internal.jsp" %>
<jsp:useBean id="nuget_teamcity_include_controllers" scope="request" type="java.util.Collection<jetbrains.buildServer.nuget.server.settings.SettingsSection >"/>
<jsp:useBean id="nuget_teamcity_include_selected" scope="request" type="jetbrains.buildServer.nuget.server.settings.SettingsSection"/>
<jsp:useBean id="nuget_teamcity_include_key" scope="request" type="java.lang.String"/>

<div id="nugetSettingsTabContainer" style="padding: 0 0 1.5em 0; display: block;" class="simpleTabs">

  <c:forEach items="${nuget_teamcity_include_controllers}" var="nugetSettingsPage" varStatus="step">
    <c:choose>
      <c:when test="${nuget_teamcity_include_selected.sectionId eq nugetSettingsPage.sectionId}">
        <strong><c:out value="${nugetSettingsPage.sectionName}"/></strong>
      </c:when>
      <c:otherwise>
        <a href="<c:url value='/admin/admin.html?item=nugetServerSettingsTab&${nuget_teamcity_include_key}=${nugetSettingsPage.sectionId}'/>"><c:out value="${nugetSettingsPage.sectionName}"/></a>
      </c:otherwise>
    </c:choose>
    <c:if test="${not step.last}"> | </c:if>
  </c:forEach>
</div>
<div class="clr"></div>
<div style="padding-bottom: 1em"></div>

<script type="text/javascript">
  <c:url var="baseUrl" value="/admin/admin.html?item=nugetServerSettingsTab"/>
  (function(){
    var tabs = new TabbedPane();
    <c:forEach items="${nuget_teamcity_include_controllers}" var="nugetSettingsPage" varStatus="step">
    tabs.addTab('${nugetSettingsPage.sectionId}', {
      caption : '${util:forJS(nugetSettingsPage.sectionName, false, false)}',
      url : '${baseUrl}&${nuget_teamcity_include_key}=${nugetSettingsPage.sectionId}'
    });
    </c:forEach>

    tabs.showIn('nugetSettingsTabContainer');
    tabs.setActiveCaption('${nuget_teamcity_include_selected.sectionId}');
  })();
</script>

<!-- start of NuGet Settings page: <c:out value="${nuget_teamcity_include_selected}"/> -->
<jsp:include page="${nuget_teamcity_include_selected.includePath}"/>
<!-- end of NuGet Settings page: <c:out value="${nuget_teamcity_include_selected}"/> -->

