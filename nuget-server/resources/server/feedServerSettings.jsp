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
<jsp:useBean id="serverStatus" scope="request" type="jetbrains.buildServer.nuget.server.feed.server.NuGetServerStatus" />
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.server.feed.server.tab.FeedServerContants"/>


<h2 class="noBorder">TeamCity as NuGet Feed</h2>
<div style="width: 50em;">
<p>In this section you may select if you like to make TeamCity be a NuGet feed.</p>

<form id="nugetSettingsForm" action="<c:url value='${nugetSettingsPostUrl}'/>" method="post">
  <table class="runnerFormTable">
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
    <tr>
      <th>Enable NuGet Server:</th>
      <td>
        <forms:saving id="serverStatusIcon"/>
        <props:checkboxProperty name="${fb.nugetServerEnabledCheckbox}" onclick="BS.NuGet.FeedServer.persistCheckbox()"/> Enabled NuGet Server
        <span class="smallNote">Enabled or disabled nuget feed server running inside TeamCity</span>
      </td>
    </tr>
  </table>
</form>

  <c:set var="nugetStatusRefreshFullUrl"><c:url value="${nugetStatusRefreshUrl}"/></c:set>
  <bs:refreshable containerId="nugetServerStatus" pageUrl="${nugetStatusRefreshFullUrl}">
    <table class="runnerFormTable">
      <tr>
        <th>NuGet Server status:</th>
        <td style="padding-top: 12px;">
          <c:choose>
            <c:when test="${serverStatus.scheduledToStart}">
              <div style="">Server is starting</div>
              <span class="smallNote">NuGet Feed server is not runnig now and will be started soon.</span>
            </c:when>

            <c:when test="${not serverStatus.running}">
              <div style="">Server is stopped</div>
            </c:when>

            <%-- server is runnning --%>
            <c:when test="${empty serverStatus.serverAccessible}">
              <div style="">Server is starting</div>
            </c:when>

            <c:when test="${not serverStatus.serverAccessible}">
              <div style="">Ping Failed</div>
              <span class="smallNote">Check TeamCity Server Url is accessible from localhost</span>
            </c:when>
            <c:when test="${serverStatus.serverAccessible}">
              <div style="">Running</div>
              <span class="smallNote">NuGet Feed server is running now.</span>
            </c:when>
          </c:choose>
        </td>
      </tr>
    </table>
  </bs:refreshable>
</div>


<script type="text/javascript">
  if (!BS) BS = {};
  if (!BS.NuGet) BS.NuGet = {};

  BS.NuGet.FeedServer = {
    Form : OO.extend(BS.AbstractWebForm, {
      formElement : function() {
        return $('nugetSettingsForm');
      },

      saveFormOnCheckbox : function() {
        BS.Util.show($('serverStatusIcon'));

        var that = this;
        BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
          onCompleteSave: function() {
            BS.Util.reenableForm(that.formElement());
            BS.Util.hide($('serverStatusIcon'));
            BS.NuGet.FeedServer.refreshStatus();
          }
        }));
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
    }
  };

  Event.observe(window, "load", function() { BS.NuGet.FeedServer.registerStatusRefresh(); });
</script>











