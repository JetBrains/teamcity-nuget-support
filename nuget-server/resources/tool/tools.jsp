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
<jsp:useBean id="installerUrl" type="java.lang.String" scope="request"/>

<c:set var="installedPluginsCount" value="${fn:length(tools.installed)}"/>
<p>
  TeamCity NuGet plugin requires to configure NuGet.Exe Command Line clients.
  There are
  <strong><c:out value="${installedPluginsCount}"/></strong>
  plugin<bs:s val="${installedPluginsCount}"/> installed.
</p>

<h2 class="noBorder">Installed NuGet Versions</h2>
<c:choose>
  <c:when test="${fn:length(tools.installed) eq 0}">
    <div>There are no installed NuGet.exe</div>
  </c:when>
  <c:otherwise>
    <c:forEach var="tool" items="${tools.installed}">
      <div>
        NuGet version: <c:out value="${tool.version}"/>
      </div>
    </c:forEach>
  </c:otherwise>
</c:choose>

<div class="addNew">
  <a href="#" onclick="return BS.NuGet.InstallPopup.show();">
    Install NuGet.exe Command Line client
    <forms:saving id="nugetInstallLinkSaving"/>
  </a>
</div>

<script type="text/javascript">
  if (!BS) BS = {};
  if (!BS.NuGet) BS.NuGet = {};
  BS.NuGet.InstallPopup = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
      return $('nugetInstallFormDialog');
    },

    show : function() {
      var that = this;
      that.showCentered();
      $('nugetInstallFormResresh').refresh("nugetInstallLinkSaving", null, function() {
          that.showCentered();
      });

      return false;
    },

    save : function() {
      alert('save');
    }
  });
</script>

<bs:modalDialog
        formId="nugetInstallForm"
        title="Install NuGet.exe Command Line"
        action="foo.html"
        closeCommand="BS.NuGet.InstallPopup.close();"
        saveCommand="BS.NuGet.InstallPopup.save();">
  <c:set var="actualInstallerUrl"><c:url value="${installerUrl}"/></c:set>
  <bs:refreshable containerId="nugetInstallFormResresh" pageUrl="${actualInstallerUrl}">
    <jsp:include page="installTool-loading.jsp"/>
  </bs:refreshable>
  <div class="popupSaveButtonsBlock">
    <a href="javascript://" onclick="BS.NuGet.InstallPopup.close();" class="cancel">Close</a>
    <br clear="all"/>
  </div>

</bs:modalDialog>