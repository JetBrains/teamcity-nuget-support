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
<jsp:useBean id="tools" type="java.util.Collection<jetbrains.buildServer.nuget.server.toolRegistry.tab.LocalTool>" scope="request"/>
<jsp:useBean id="installerUrl" type="java.lang.String" scope="request"/>
<jsp:useBean id="updateUrl" type="java.lang.String" scope="request"/>

<c:set var="actualUpdateUrl"><c:url value="${updateUrl}"/></c:set>
<bs:refreshable containerId="nugetPackagesList" pageUrl="${actualUpdateUrl}">
<c:set var="installedPluginsCount" value="${fn:length(tools)}"/>

<p style="width: 40em;">
  Listed NuGet versions are automatically distributed to all build agents and can be used in NuGet-related runners.
</p>
<p>
  There <bs:are_is val="${installedPluginsCount}"/>
  <strong><c:out value="${installedPluginsCount}"/></strong>
  NuGet<bs:s val="${installedPluginsCount}"/> installed.
</p>

  <c:if test="${not (installedPluginsCount eq 0)}">
      <table class="dark borderBottom" cellpadding="0" cellspacing="0" style="width: 30em;">
        <thead>
        <tr>
          <th class="header" style="width: 66%">Version</th>
          <th class="header"></th>
        </tr>
        </thead>
        <tbody>
          <c:forEach var="tool" items="${tools}">
            <tr>
              <td><c:out value="${tool.version}"/></td>
              <td>
                <c:choose>
                  <c:when test="${tool.state.installed}">
                    <a href="#" onclick="BS.NuGet.Tools.removeTool('<bs:forJs>${tool.id}</bs:forJs>');">Remove</a>
                  </c:when>
                  <c:when test="${tool.state.installing}">
                    <bs:commentIcon text="Messages"/>
                    Installing...
                  </c:when>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
    </table>
  </c:if>
  <div style="margin-top: 1em;">
    <forms:addButton onclick="BS.NuGet.Tools.InstallPopup.show()">
      Download NuGet
    </forms:addButton>
  </div>
</bs:refreshable>


<c:set var="actualInstallerUrl"><c:url value="${installerUrl}"/></c:set>
<bs:modalDialog
        formId="nugetInstallForm"
        title="Download NuGet"
        action="${actualInstallerUrl}"
        closeCommand="BS.NuGet.Tools.InstallPopup.close();"
        saveCommand="BS.NuGet.Tools.InstallPopup.save();">
  <div id="nugetInstallFormLoading">
    <forms:saving style="float: left; display:block;"/>
    Fetching available NuGet versions from NuGet.org
  </div>

  <bs:refreshable containerId="nugetInstallFormResresh" pageUrl="${actualInstallerUrl}">

  </bs:refreshable>

  <div class="popupSaveButtonsBlock">
    <forms:cancel onclick="BS.NuGet.Tools.InstallPopup.closeToolsDialog();"/>
    <forms:submit id="installNuGetApplyButton" label="Install"/>
    <input type="button" class="btn cancel" onclick="BS.NuGet.Tools.InstallPopup.refreshForm(true);" value="Refresh"/>
    <forms:saving id="installNuGetApplyProgress"/>
    <div class="clr"></div>
  </div>
</bs:modalDialog>

<script type="text/javascript">
  BS.NuGet.Tools.installUrl = "<bs:forJs>${actualInstallerUrl}</bs:forJs>";
</script>
