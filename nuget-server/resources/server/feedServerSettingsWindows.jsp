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

<jsp:useBean id="serverUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="nugetStatusRefreshUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="nugetSettingsPostUrl" scope="request" type="java.lang.String" />
<jsp:useBean id="imagesBase" scope="request" type="java.lang.String" />
<jsp:useBean id="serverStatus" scope="request" type="jetbrains.buildServer.nuget.server.feed.server.NuGetServerStatus" />
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.server.feed.server.tab.FeedServerContants"/>

<c:set var="nugetStatusRefreshFullUrl"><c:url value="${nugetStatusRefreshUrl}"/></c:set>

<h2 class="noBorder">TeamCity as NuGet Feed</h2>
<div style="width: 50em; margin-bottom: 3em;">
<p>In this section you may select if you like to make TeamCity be a NuGet feed.</p>

<form id="nugetSettingsForm" action="<c:url value='${nugetSettingsPostUrl}'/>" method="post" onsubmit="return BS.NuGet.FeedServer.Form.saveForm();">
  <table class="runnerFormTable">
    <tr>
      <th>Enable NuGet Server:</th>
      <td>
        <props:checkboxProperty name="${fb.nugetServerEnabledCheckbox}"/> Enabled NuGet Server
        <span class="smallNote">Enables or disables NuGet feed server running inside TeamCity</span>
      </td>
    </tr>
    <tr>
      <th rowspan="2">TeamCity Url:</th>
      <td>
        <props:textProperty name="${fb.nugetServerUrl}" className="longField"/>
        <span class="smallNote">Specify URL or TeamCity server for internally
          running NuGet server process. Leave blank to use TeamCity server URL(${serverUrl})</span>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <div class="attentionComment">
          Server URL<bs:help file="Configuring+Server+URL"/> is <strong>${serverUrl}</strong>.
          It will be used by NuGet Server process to connect to TeamCity server to fetch data.
          Make sure this URL is available for localhost connections.
          To change it use <a href="<c:url value='/admin/serverConfig.html?init=1'/>" target="_blank">Server Configuration page</a>.
        </div>
      </td>
    </tr>
  </table>

  <div id="nugetSettingsSuccessMessage" class="successMessage">NuGet server settings saved.</div>

  <div class="saveButtonsBlock" style="border: none;">
     <input class="submitButton" type="submit" value="Save">
     <input type="hidden" id="submitSettings" name="submitSettings" value="store"/>
     <forms:saving id="nugetSettingsSaving"/>
   </div>

  <div class="clr"></div>
</form>
</div>


<h2 class="noBorder">NuGet Server status:</h2>
<p></p>

<bs:refreshable containerId="nugetServerStatus" pageUrl="${nugetStatusRefreshFullUrl}">
  <c:choose>
    <c:when test="${serverStatus.scheduledToStart}">
      <div style="">
        <img src="<c:url value='${imagesBase}/restarting.gif'/>" alt="starting"/>
        Server is starting
      </div>
      <span class="smallNote">NuGet Feed server is not runnig now and will be started soon.</span>
    </c:when>

    <c:when test="${not serverStatus.running}">
      <div style="">
        <img src="<c:url value='${imagesBase}/stopped.gif'/>" alt="stopped"/>
        Server is stopped
      </div>
    </c:when>

    <%-- server is runnning --%>
    <c:when test="${empty serverStatus.serverAccessible}">
      <div style="">
        <img src="<c:url value='${imagesBase}/starting.gif'/>" alt="starting"/>
        Server is starting
      </div>
    </c:when>

    <c:when test="${not serverStatus.serverAccessible}">
      <div style="">
        <img src="<c:url value='${imagesBase}/error.gif'/>" alt="error"/>
        Ping Failed
      </div>
      <span class="smallNote">Check TeamCity Server Url is accessible from localhost</span>
    </c:when>
    <c:when test="${serverStatus.serverAccessible}">
      <div style="">
        <img src="<c:url value='${imagesBase}/running.gif'/>" alt="starting"/>
        Running
      </div>
      <span class="smallNote">NuGet Feed server is running now.</span>
    </c:when>
  </c:choose>
</bs:refreshable>

<h3>Recent NuGet Server log:</h3>
<bs:refreshable containerId="nugetServerLogs" pageUrl="${nugetStatusRefreshFullUrl}">
  <div style="width: 75%">
    <pre id="nugetServerLogView" style="padding: 1em; font-size: 90%; overflow: auto; height: 20em; background-color: #eee;"><c:out value="${serverStatus.logsSlice}"/></pre>
    <a href="<c:url value='/admin/serverConfig.html?tab=diagnostic&init=1&subTab=logs'/>">See all server logs </a>
    |
    <a href="<c:url value='/get/file/serverLogs/teamcity-nuget-server.log'/>">Download full log</a>
    |
    <a href="#" onclick="BS.NuGet.FeedServer.refreshLog(); return false;">Refresh</a>
  </div>
  <script type="text/javascript">
    var el = $('nugetServerLogView');
    el.scrollTop = el.scrollHeight;
  </script>
</bs:refreshable>


<script type="text/javascript">
  if (!BS) BS = {};
  if (!BS.NuGet) BS.NuGet = {};

  BS.NuGet.FeedServer = {
    Form : OO.extend(BS.AbstractWebForm, {
      formElement : function() {
        return $('nugetSettingsForm');
      },

      saveForm : function() {
        var that = this;
        BS.Util.show($('nugetSettingsSaving'));
        BS.Util.hide($('nugetSettingsSuccessMessage'));
        BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
          onCompleteSave: function() {
            BS.Util.hide($('nugetSettingsSaving'));
            BS.Util.reenableForm(that.formElement());
            BS.NuGet.FeedServer.refreshStatus();
            BS.Util.show($('nugetSettingsSuccessMessage'));
          }
        }));
        return false;
      }
    }),

    refreshStatus : function() {
      $('nugetServerStatus').refresh();
    },

    registerStatusRefresh : function() {
      var that = this;
      setTimeout(function() {
        that.refreshStatus();
        that.registerStatusRefresh();
      }, 1000);
    },

    persistCheckbox : function() {
      setTimeout(function() {
        BS.NuGet.FeedServer.Form.saveFormOnCheckbox();
      }, 100);
    },

    refreshLog : function() {
      $('nugetServerLogs').refresh();
    }
  };

  Event.observe(window, "load", function() {
    BS.NuGet.FeedServer.registerStatusRefresh();
    BS.Util.hide($('nugetSettingsSuccessMessage'));
  });
</script>

