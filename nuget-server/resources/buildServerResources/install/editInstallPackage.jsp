

<%@ include file="/include.jsp"%>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.install.InstallBean" scope="request"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<jsp:include page="/tools/editToolUsage.html?toolType=${ib.nugetToolTypeName}&versionParameterName=${ib.nuGetPathKey}&class=longField"/>

<script type="text/javascript">
    var restoreModeId = BS.Util.escapeId('${ib.restoreCommandModeKey}');

    BS.NugetParametersForm = {
        updateElements: function () {
            var restoreMode = $j(restoreModeId).val();
            $j(BS.Util.escapeId('exclude-version')).toggleClass('hidden', restoreMode === '${ib.restoreCommandModeRestoreValue}');

            BS.MultilineProperties.updateVisible();
        },
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
    };

    $j(document).on('change', restoreModeId, function () {
        BS.NugetParametersForm.updateElements();
    });

    $j(document).on('ready', restoreModeId, function () {
        BS.NugetParametersForm.updateElements();
    });
</script>

<l:settingsGroup title="Restore Packages">
  <tr>
    <th>Path to solution file<l:star/>:</th>
    <td>
      <props:textProperty name="${ib.solutionPathKey}" className="longField">
      <jsp:attribute name="afterTextField"><bs:vcsTree fieldId="${ib.solutionPathKey}"/></jsp:attribute>
      </props:textProperty>
      <span class="smallNote">The path to Visual Studio solution file (.sln)</span>
      <span class="error" id="error_${ib.solutionPathKey}"></span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th><label for="${ib.restoreCommandModeKey}">Restore mode:</label></th>
    <td>
      <c:set var="restoreMode" value="${propertiesBean.properties[ib.restoreCommandModeKey]}"/>
      <props:selectProperty name="${ib.restoreCommandModeKey}" style="longField">
        <props:option value="${ib.restoreCommandModeRestoreValue}">Restore (requires NuGet 2.7+)</props:option>
        <props:option value="${ib.restoreCommandModeInstallValue}" selected="${empty restoreMode or (restoreMode eq ib.restoreCommandModeInstallValue)}">Install</props:option>
      </props:selectProperty>
      <span class="smallNote">Select <em>NuGet.exe restore</em> or <em>NuGet.exe install</em> command to restore packages for the solution</span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th>Restore options:</th>
    <td>
      <div id="exclude-version">
        <props:checkboxProperty name="${ib.excludeVersionKey}"/>
        <label for="${ib.excludeVersionKey}">Exclude version from package folder names</label>
        <span class="smallNote">Makes NuGet exclude package version from package folder names.
                                Equivalent to the <em>-ExcludeVersion</em> NuGet.exe command line argument</span><br/>
      </div>
      <props:checkboxProperty name="${ib.noCacheKey}"/>
      <label for="${ib.noCacheKey}">Disable looking up packages from local machine cache</label>
      <span class="smallNote">Equivalent to the <em>-NoCache</em> NuGet.exe commanline argument</span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th><label for="${ib.restoreCustomCommandline}">Command line parameters:</label></th>
    <td>
      <props:textProperty name="${ib.restoreCustomCommandline}" className="longField" expandable="true"/>
      <span class="smallNote">Enter additional parameters to use when calling <a href="https://docs.microsoft.com/en-us/nuget/tools/cli-ref-restore">nuget restore</a> command</span>
      <span id="error_${ib.restoreCustomCommandline}" class="error"></span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Packages Sources" className="advancedSetting">
  <tr class="advancedSetting">
    <th>Packages sources:</th>
    <td>
      <props:multilineProperty name="${ib.nuGetSourcesKey}"
                               linkTitle="Sources"
                               cols="60" rows="5"
                               expanded="${true}"/>
      <button id="queryString" style="display: none"></button>
      <bs:projectData type="NuGetFeedUrls" sourceFieldId="queryString"
                      targetFieldId="${ib.nuGetSourcesKey}" popupTitle="Select TeamCity NuGet feeds"/>
      <span class="smallNote">
        Leave blank to use NuGet.org<br />
        To use a TeamCity NuGet feed<bs:help file="Using+TeamCity+as+NuGet+Server"/>, specify the URL from the NuGet feed project settings page.<br />
        For feeds with authentication configure the <em>NuGet Feed Credentials</em> build feature
        <bs:help file="NuGet+Feed+Credentials"/>
      </span>
      <span class="error" id="error_${ib.nuGetSourcesKey}"></span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Update Packages" className="advancedSetting">
  <tr class="advancedSetting">
    <th>Update packages:</th>
    <td>
      <props:checkboxProperty name="${ib.updatePackagesKey}"/>
      <label for="${ib.updatePackagesKey}">Update packages with help of NuGet update command</label>
      <span class="smallNote">Uses the NuGet <em>update</em> command to update all packages under solution.
                              Package versions and constraints are taken from
                              <em>packages.config</em> files</span>
    </td>
  </tr>
  <tr id="nugetUpdateModeSection" class="advancedSetting">
    <th>Update mode:</th>
    <td>
      <props:selectProperty name="${ib.updateModeKey}" style="longField">
        <props:option value="${ib.updatePerSolutionValue}">Update via solution file</props:option>
        <props:option value="${ib.updatePerConfigValue}">Update via packages.config file</props:option>
      </props:selectProperty>
      <span class="smallNote">
        Select how to update packages:
        via a call to
        <em>NuGet.exe update SolutionFile.sln</em> or
        via calls to
        <em>NuGet.exe update Packages.Config</em> for each <em>packages.config</em> file under the solution.
      </span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th rowspan="2">Update options:</th>
    <td>
      <props:checkboxProperty name="${ib.updatePackagesPrerelease}"/>
      <label for="${ib.updatePackagesPrerelease}">Include pre-release packages</label>
      <span class="smallNote">Equivalent to the -Prerelease NuGet.exe option</span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <td>
      <props:checkboxProperty name="${ib.updatePackagesSafeKey}"/>
      <label for="${ib.updatePackagesSafeKey}">Perform safe update</label>
      <span class="smallNote">Equivalent to the -Safe NuGet.exe option</span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th><label for="${ib.updateCustomCommandline}">Command line parameters:</label></th>
    <td>
      <props:textProperty name="${ib.updateCustomCommandline}" className="longField" expandable="true"/>
      <span class="smallNote">Enter additional parameters to use when calling <a href="https://docs.microsoft.com/en-us/nuget/tools/cli-ref-update">nuget update</a> command</span>
      <span id="error_${ib.updateCustomCommandline}" class="error"></span>
    </td>
  </tr>
</l:settingsGroup>

<script type="text/javascript">
    BS.NugetParametersForm.updateElements();
    $('queryString').value = encodeURIComponent(BS.NugetParametersForm.getFeedUrlQueryString());
</script>
