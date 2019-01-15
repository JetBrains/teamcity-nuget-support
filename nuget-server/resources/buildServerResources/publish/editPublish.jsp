<%--
  ~ Copyright 2000-2015 JetBrains s.r.o.
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

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.publish.PublishBean" scope="request"/>

<jsp:include page="/tools/editToolUsage.html?toolType=${ib.nugetToolTypeName}&versionParameterName=${ib.nuGetPathKey}&class=longField"/>

<script type="text/javascript">
  appendPackageToUpload = function(packageFile) {
    var textarea = $j(BS.Util.escapeId('${ib.nuGetPublishFilesKey}'));
    var val = textarea.text();
    if (val.length > 0) {
      var lines = val.split("\n");
      lines.push(packageFile);
      textarea.text(lines.join("\n"));
    } else {
      textarea.text(packageFile);
    }
  };
  BS.NugetParametersForm = {
      getFeedUrlQueryString: function () {
          var parameters = {
              apiVersions: "v2;v3"
          };
          var search = window.location.search.substring(1).split('&');
          search.forEach(function (value) {
              var buildTypeMatch = value.match(/id=buildType:(.*)/);
              if (buildTypeMatch) {
                  parameters["buildType"] = buildTypeMatch[1]
              }
              var templateMatch = value.match(/id=template:(.*)/);
              if (templateMatch) {
                  parameters["template"] = templateMatch[1]
              }
          });

          return Object.keys(parameters).reduce(function (previous, key) {
              if (previous) {
                  previous += "&";
              }
              return previous + key + "=" + parameters[key];
          }, "");
      }
  }
</script>

<tr>
  <th>Packages<l:star/>:</th>
  <td>
    <props:multilineProperty name="${ib.nuGetPublishFilesKey}" linkTitle="Package files"
                             cols="60" rows="5"
                             expanded="${true}">
      <jsp:attribute name="afterTextField"><bs:vcsTree callback="appendPackageToUpload" treeId="${ib.nuGetPublishFilesKey}"/></jsp:attribute>
    </props:multilineProperty>    
    <script type="text/javascript">
      BS.Util.hide($('vcsTreeControl_${ib.nuGetPublishFilesKey}'));
    </script>
    <span class="smallNote">A newline-separated list of NuGet package files (.nupkg) to push to the NuGet feed. Wildcards are supported.</span>
    <span class="error" id="error_${ib.nuGetPublishFilesKey}"></span>
  </td>
</tr>

<tr>
  <th>API key:</th>
  <td>
    <props:passwordProperty name="${ib.apiKey}" className="longField"/>
      <span class="smallNote">
        Specify the API key to access a NuGet packages feed.<br />
        For built-in TeamCity NuGet server, specify
        <em><c:out value="${ib.nuGetFeedApiKeyReference}"/></em>
      </span>
    <span class="error" id="error_${ib.apiKey}"></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th>Package source:</th>
  <td>
    <props:textProperty name="${ib.nuGetSourceKey}" className="longField"/>
    <button id="queryString" style="display: none"></button>
    <bs:projectData type="NuGetFeedUrls" sourceFieldId="queryString" selectionMode="single"
                    targetFieldId="${ib.nuGetSourceKey}" popupTitle="Select TeamCity NuGet feed" />
      <span class="smallNote">
        Specify the NuGet packages feed URL to push packages to. Leave blank to let NuGet decide what package repository to use.<br />
        To use a TeamCity NuGet feed<bs:help file="Using+TeamCity+as+NuGet+Server"/>, specify the URL from the NuGet feed project settings page.
      </span>
    <span class="error" id="error_${ib.nuGetSourceKey}"></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${ib.pushCustomCommandline}">Command line parameters:</label></th>
  <td>
    <props:textProperty name="${ib.pushCustomCommandline}" className="longField" expandable="true"/>
    <span class="smallNote">Enter additional parameters to use when calling <a href="https://docs.microsoft.com/en-us/nuget/tools/cli-ref-push">nuget push</a> command</span>
    <span id="error_${ib.pushCustomCommandline}" class="error"></span>
  </td>
</tr>

<script type="text/javascript">
  $j(document).ready(function() {
    //move vcs-tree icon to the left from textarea, after completion icon
    var img = $('vcsTreeControl_${ib.nuGetPublishFilesKey}');
    if (!img) {
      //there is no icon when no vcs roots configured
      return;
    }
    img.remove();
    img.style.position = 'absolute';

    var textarea = $('${ib.nuGetPublishFilesKey}');
    var dim = textarea.getDimensions(),
        layout = textarea.getLayout();

    var xshift = dim.width + 20; // Put next to the completion icon
    var pos = textarea.positionedOffset();

    textarea.parentNode.appendChild(img);
    var x = pos[0] + xshift + layout.get('margin-left');
    var y = pos[1] + 3 + layout.get('margin-top');
    BS.Util.show(img);
    BS.Util.place(img, x, y);

    $('queryString').value = encodeURIComponent(BS.NugetParametersForm.getFeedUrlQueryString());
  });
</script>
