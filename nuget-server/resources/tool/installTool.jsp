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
<jsp:useBean id="installTools" type="jetbrains.buildServer.nuget.server.toolRegistry.tab.InstallToolBean" scope="request"/>

<div id="nugetInstallFormResreshInner">
  <table class="runnerFormTable">
    <tr>
      <td colspan="2">
        Specify NuGet command line to install:
      </td>
    </tr>
    <tr>
      <th><label for="toolId">NuGet:<l:star/></label></th>
      <td>
        <input type="hidden" name="whatToDo" value="install" />
        <forms:select name="toolId" style="width:15em;">
          <forms:option value="">-- Please choose version --</forms:option>
          <optgroup label="Public builds">
            <c:forEach var="t" items="${installTools.tools}">
              <forms:option value="${t.id}"><c:out value="${t.version}"/></forms:option>
            </c:forEach>
          </optgroup>
          <optgroup label="Custom">
            <forms:option value="custom">Upload</forms:option>
          </optgroup>
        </forms:select>
        <span class="error" id="error_toolId"></span>
      </td>
    </tr>

    <tr id="nugetUploadRow">
      <th><label for="nugetUploadControl">Select NuGet to upload</label></th>
      <td>
        <forms:file name="nugetUploadControl"/>
        <span class="smallNote">
          You may download a custom build of NuGet.CommandLine package and upload it here.
          Make sure package is named like <em>NuGet.CommandLine.x.y.z.nupkg</em>
        </span>
      </td>
    </tr>

    <script type="text/javascript">
      (function() {
        var updateUploadRow = function() {
          if ($j('#toolId').val() == 'custom') {
            BS.Util.show($('nugetUploadRow'));
          } else {
            BS.Util.hide($('nugetUploadRow'));
          }
        };

        $j('#toolId').change(updateUploadRow);
        updateUploadRow();
      })();
    </script>

    <c:if test="${installTools.showErrors}">
    <tr>
      <td colspan="2">
        <c:choose>
          <c:when test="${not empty installTools.errorText}">
            <div class="error" style="margin-left: 0">
              <div>
                Failed to fetch latest NuGet from default feed.
              </div>
              <div>
                <c:out value="${installTools.errorText}"/>
              </div>
            </div>
          </c:when>
          <c:when test="${fn:length(installTools.tools) eq 0}">
            <div>No other NuGet command line versions available</div>
          </c:when>
        </c:choose>
      </td>
    </tr>
    </c:if>
  </table>
</div>
