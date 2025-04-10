

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
        To use a TeamCity NuGet feed<bs:help file="Using+TeamCity+as+NuGet+Feed"/>, specify the URL from the NuGet feed project settings page.
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
    $('queryString').value = encodeURIComponent(BS.NugetParametersForm.getFeedUrlQueryString());
  });
</script>
