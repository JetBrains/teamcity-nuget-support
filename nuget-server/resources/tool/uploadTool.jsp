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
  <input type="hidden" name="toolId" value="custom" />

  <table class="runnerFormTable">
    <tr>
      <td colspan="2">
        Select NuGet package to upload:
      </td>
    </tr>
    <tr id="nugetUploadRow">
      <th><label for="nugetUploadControl">Upload:<l:star/></label></th>
      <td>
        <forms:file name="nugetUploadControl"/>
        <span class="smallNote">
          Specify path to NuGet package (.nupkg file) with <em>tools/NuGet.exe</em> file inside.
        </span>
        <span class="smallNote">Download <em>NuGet.Commandline.&lt;VERSION&gt;.nupkg</em> file from
          <a href="http://nuget.org/packages/NuGet.CommandLine" target="_blank">NuGet.org</a> and upload it here</span>
        <span class="error" id="error_toolId"></span>
      </td>
    </tr>
    <jsp:include page="toolOptions.jsp"/>
  </table>

</div>
