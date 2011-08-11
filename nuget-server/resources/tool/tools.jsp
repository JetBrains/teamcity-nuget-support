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

<c:set var="installedPluginsCount" value="${fn:length(tools)}"/>
<p>
  TeamCity NuGet plugin requires to configure NuGet.Exe Command Line clients.
  There are
  <strong><c:out value="${installedPluginsCount}"/></strong>
  plugin<bs:s val="${installedPluginsCount}"/> installed.
</p>

<h2 class="noBorder">Installed NuGet Versions</h2>
<c:choose>
  <c:when test="${installedPluginsCount eq 0}">
    <div>There are no installed NuGet.exe</div>
  </c:when>
  <c:otherwise>
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
                    <a href="#">Remove</a>
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
  </c:otherwise>
</c:choose>

<div class="addNew">
  <a href="#" onclick="return BS.NuGet.InstallPopup.show();">
    Install
    <c:if test="${installedPluginsCount gt 0}">addintional versions of</c:if>
    NuGet.exe Command Line
    <forms:saving id="nugetInstallLinkSaving"/>
  </a>
</div>

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
    <a href="javascript://" onclick="BS.NuGet.InstallPopup.close();" class="cancel">Cancel</a>
    <input class="submitButton" type="button" value="Install" id="agentPoolNameApplyButton" onclick="BS.NuGet.InstallPopup.save();"/>
    <br clear="all"/>
  </div>
</bs:modalDialog>