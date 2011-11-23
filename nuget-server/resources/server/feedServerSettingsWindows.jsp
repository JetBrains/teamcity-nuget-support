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
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="serverUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="nugetStatusRefreshUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="nugetSettingsPostUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="serverEnabled" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.server.feed.server.tab.FeedServerContants"/>

<c:set var="nugetStatusRefreshFullUrl"><c:url value="${nugetStatusRefreshUrl}"/></c:set>
<c:set var="nugetSettingsPostFullUrl"><c:url value="${nugetSettingsPostUrl}"/></c:set>

<bs:refreshable containerId="nugetEnableDisable" pageUrl="${nugetStatusRefreshFullUrl}">
  <div style="padding-top:1em;">
    NuGet Server is
    <c:choose>
      <c:when test="${serverEnabled}">
        <strong>enabled</strong> <input type="button" value="Disable" onclick="return BS.NuGet.FeedServer.disableFeedServer();" />
      </c:when>
      <c:otherwise>
        <strong>disabled</strong> <input type="button" value="Enable" onclick="return BS.NuGet.FeedServer.enableFeedServer();" />
      </c:otherwise>
    </c:choose>
  </div>
</bs:refreshable>

<jsp:include page="feedServerStatus.jsp"/>


<bs:modalDialog formId="nugetEnableFeed"
                title="Configure NuGet Server"
                action="${nugetSettingsPostFullUrl}"
                closeCommand="BS.NuGet.FeedServer.EnableForm.close();"
                saveCommand="BS.NuGet.FeedServer.EnableForm.saveForm();">
  <props:hiddenProperty name="${fb.nugetServerEnabledCheckbox}" value="true"/>

   <table class="runnerFormTable">
    <tr>
      <th style="width: 8em;">TeamCity Url:</th>
      <td>
        <props:textProperty name="${fb.nugetServerUrl}" className="longField"/>
        <span class="smallNote">
          Specify URL or TeamCity server for internally running
          NuGet server process. Leave blank to use TeamCity server URL(${serverUrl})
        </span>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <div class="attentionComment">
          Server URL<bs:help file="Configuring+Server+URL"/> is <strong>${serverUrl}</strong>.
          It will be used by NuGet Server process to connect to TeamCity server to fetch data.
          Make sure this URL is available for localhost connections.
        </div>
      </td>
    </tr>
  </table>

  <div class="saveButtonsBlock" style="border: none; margin-top: 1em;">
    <a href="#" class="cancel" onclick="return BS.NuGet.FeedServer.EnableForm.close();">Cancel</a>
    <input class="submitButton" type="submit" value="Enable">
    <forms:saving id="nugetSettingsSaving"/>
  </div>

  <div class="clr"></div>
</bs:modalDialog>

<bs:modalDialog formId="nugetDisableFeed"
                title="Configure NuGet Server"
                action="${nugetSettingsPostFullUrl}"
                closeCommand="BS.NuGet.FeedServer.DisableForm.close();"
                saveCommand="BS.NuGet.FeedServer.DisableForm.saveForm();">
  <props:hiddenProperty name="${fb.nugetServerEnabledCheckbox}" value="false"/>

  You are going to stop NuGet Feed Server that was running inside TeamCity. Continue?

  <div class="saveButtonsBlock" style="border: none; margin-top: 1em;">
    <a href="#" class="cancel" onclick="return BS.NuGet.FeedServer.DisableForm.close();">Cancel</a>
    <input class="submitButton" type="submit" value="Disable">
    <forms:saving id="nugetSettingsSaving"/>
  </div>

  <div class="clr"></div>
</bs:modalDialog>


