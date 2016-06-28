<%--
  ~ Copyright 2000-2014 JetBrains s.r.o.
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

<%@ include file="/include.jsp"%>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.install.InstallBean" scope="request"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<jsp:include page="/tools/editToolUsage.html?toolType=${ib.nugetToolTypeName}&versionParameterName=${ib.nuGetPathKey}&class=longField"/>

<l:settingsGroup title="Restore Packages">
  <tr>
    <th>Path To Solution File<l:star/>:</th>
    <td>
      <props:textProperty name="${ib.solutionPathKey}" className="longField"/>
      <bs:vcsTree fieldId="${ib.solutionPathKey}"/>
      <span class="smallNote">The path to Visual Studio solution file (.sln)</span>
      <span class="error" id="error_${ib.solutionPathKey}"></span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th><label for="${ib.restoreCommandModeKey}">Restore Mode:</label></th>
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
    <th rowspan="2">Restore Options:</th>
    <td>
      <props:checkboxProperty name="${ib.excludeVersionKey}"/>
      <label for="${ib.excludeVersionKey}">Exclude version from package folder names</label>
      <span class="smallNote">Makes NuGet exclude package version from package folder names.
                              Equivalent to the <em>-ExcludeVersion</em> NuGet.exe command line argument</span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <td>
      <props:checkboxProperty name="${ib.noCacheKey}"/>
      <label for="${ib.noCacheKey}">Disable looking up packages from local machine cache</label>
      <span class="smallNote">Equivalent to the <em>-NoCache</em> NuGet.exe commanline argument</span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th><label for="${ib.restoreCustomCommandline}">Command line parameters:</label></th>
    <td>
      <props:textProperty name="${ib.restoreCustomCommandline}" className="longField" expandable="true"/>
      <span class="smallNote">Enter additional parameters to use when calling <a href="http://docs.nuget.org/docs/reference/command-line-reference#Restore_command">nuget restore</a> command</span>
      <span id="error_${ib.restoreCustomCommandline}" class="error"></span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Packages Sources" className="advancedSetting">
  <tr class="advancedSetting">
    <th>Packages Sources:</th>
    <td>
      <props:multilineProperty name="${ib.nuGetSourcesKey}"
                               linkTitle="Sources"
                               cols="60" rows="5"
                               expanded="${true}"/>
      <span class="smallNote">
        Leave blank to use NuGet.org
        <br />
        To use TeamCity as a NuGet server, specify
        <em><c:out value="${ib.nuGetFeedReference}"/></em>
        to refer to the TeamCity-provided guest-visible packages source
        <br />
        Specify <em><c:out value="${ib.nuGetAuthFeedReference}"/></em>
        to refer to the TeamCity-provided authenticated NuGet feed and configure the <em>NuGet Feed Credentials</em> build feature
        <bs:help file="NuGet+Feed+Credentials"/>
      </span>
      <span class="error" id="error_${ib.nuGetSourcesKey}"></span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Update Packages" className="advancedSetting">
  <tr class="advancedSetting">
    <th>Update Packages:</th>
    <td>
      <props:checkboxProperty name="${ib.updatePackagesKey}"/>
      <label for="${ib.updatePackagesKey}">Update packages with help of NuGet update command</label>
      <span class="smallNote">Uses the NuGet <em>update</em> command to update all packages under solution.
                              Package versions and constraints are taken from
                              <em>packages.config</em> files</span>
    </td>
  </tr>
  <tr id="nugetUpdateModeSection" class="advancedSetting">
    <th>Update Mode:</th>
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
    <th rowspan="2">Update Options:</th>
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
      <span class="smallNote">Enter additional parameters to use when calling <a href="http://docs.nuget.org/docs/reference/command-line-reference#Update_Command">nuget update</a> command</span>
      <span id="error_${ib.updateCustomCommandline}" class="error"></span>
    </td>
  </tr>
</l:settingsGroup>
