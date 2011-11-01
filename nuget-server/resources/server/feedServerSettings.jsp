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
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.server.feed.server.tab.FeedServerContants"/>


<h2 class="noBorder">TeamCity as NuGet Feed</h2>
<div style="width: 50em;">
<p>In this section you may select if you like to make TeamCity be a NuGet feed.</p>

  <table class="runnerFormTable">
    <tr>
      <th>Enable NuGet Server:</th>
      <td>
        <script type="text/javascript">
          if (!BS) BS = {};
          if (!BS.NuGet) BS.NuGet = {};

          BS.NuGet.FeedServer = {
            persistCheckbox : function() {

              BS.Util.show()($('serverStatusIcon'));

              VS

              BS.ajaxRequest('<bs:forJs>${nugetStatusRefreshUrl}</bs:forJs>', {
                onComplete : function() {
                  BS.Util.show($('serverStatusIcon'));
                  $('${fb.nugetServerEnabledCheckbox}').disabled = '';
                }
              })
            }
          }
        </script>
        <forms:saving id="serverStatusIcon"/>
        <props:checkboxProperty name="${fb.nugetServerEnabledCheckbox}" onclick="BS.NuGet.FeedServer.persistCheckbox()"/> Enabled NuGet Server
        <span class="smallNote">Enabled or disabled nuget feed server running inside TeamCity</span>
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

  <bs:refreshable containerId="nugetServerStatus" pageUrl="${nugetStatusRefreshUrl}">
    <table class="runnerFormTable">
      <tr>
        <td>NuGet Server status:</td>
        <td>
          <span style="color:#008000">RUNNING</span>
        </td>
      </tr>
    </table>
  </bs:refreshable>
</div>



