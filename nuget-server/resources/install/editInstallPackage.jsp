<%--
  ~ Copyright 2000-2012 JetBrains s.r.o.
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

<l:settingsGroup title="NuGet settings">
  <tr>
    <th>NuGet.exe<l:star/>:</th>
    <td>
      <jsp:include page="../tool/runnerSettings.html?name=${ib.nuGetPathKey}&class=longField"/>
    </td>
  </tr>
  <tr>
    <th>Packages Sources:</th>
    <td>
      <props:multilineProperty name="${ib.nuGetSourcesKey}"
                               linkTitle="Sources"
                               cols="60" rows="5"
                               expanded="${true}"/>
      <span class="smallNote">
        Specify NuGet package sources.
        Leave blank to use NuGet.org as packages source.
        <br />
        If you use TeamCity as NuGet server, specify
        <em><c:out value="${ib.nuGetFeedReference}"/></em>
        to refer to TeamCity provided guest-visible packages source
        <br />
        Specify <em><c:out value="${ib.nuGetAuthFeedReference}"/></em>
        to refer to TeamCity provided authenticated NuGet feed
        (you also need to configurate <em>NuGet Feed Credentials</em> build feature)
      </span>
      <span class="error" id="error_${ib.nuGetSourcesKey}"></span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Restore Packages">
  <tr>
    <th>Path to solution file:</th>
    <td>
      <props:textProperty name="${ib.solutionPathKey}" className="longField"/>
      <bs:vcsTree fieldId="${ib.solutionPathKey}"/>
      <span class="smallNote">Specify path to Visual Studio solution file (.sln)</span>
      <span class="error" id="error_${ib.solutionPathKey}"></span>
    </td>
  </tr>
  <tr>
    <th rowspan="3">Restore Options:</th>
    <td>
      <props:checkboxProperty name="${ib.restoreCommandKey}"/>
      <label for="${ib.restoreCommandKey}">Use <em>NuGet.exe restore</em> command</label>
      <span class="smallNote">Uses <em>NuGet.exe restore</em> command to install packages into solution. Only for NuGet 2.7 or newer</span>
    </td>
    <td>
      <props:checkboxProperty name="${ib.excludeVersionKey}"/>
      <label for="${ib.excludeVersionKey}">Exclude version from package folder names</label>
      <span class="smallNote">Makes NuGet exclude package version from package folders.
                              Equivalent to -ExcludeVersion NuGet.exe commandline argument</span>
    </td>
  </tr>
  <tr>
    <td>
      <props:checkboxProperty name="${ib.noCacheKey}"/>
      <label for="${ib.noCacheKey}">Disable looking up packages from local machine cache</label>
      <span class="smallNote">Equivalent to -NoCache NuGet.exe commanline argument</span>
    </td>
  </tr>
</l:settingsGroup>
<l:settingsGroup title="Update Packages">
  <tr>
    <th>Update Packages:</th>
    <td>
      <props:checkboxProperty name="${ib.updatePackagesKey}"/>
      <label for="${ib.updatePackagesKey}">Update packages with help of NuGet update command</label>
      <span class="smallNote">Uses NuGet update command to update all packages under solution.
                              Package versions and constraints are taken from
                              packages.config files</span>
    </td>
  </tr>
  <tr id="nugetUpdateModeSection">
    <th>Update Mode:</th>
    <td>
      <props:selectProperty name="${ib.updateModeKey}" style="longField">
        <props:option value="${ib.updatePerSolutionValue}">Update via solution file</props:option>
        <props:option value="${ib.updatePerConfigValue}">Update via packages.config file</props:option>
      </props:selectProperty>
      <span class="smallNote">
        NuGet.exe provides two ways of packages update. The first one is implemented via one call to
        <em>NuGet.exe update SolutionFile.sln</em>, the other one is implemented by calls to
        <em>NuGet.exe update Packages.Config</em> for each Packages.Config file under solution.
      </span>
    </td>
  </tr>
  <tr>
    <th rowspan="2">Update Options:</th>
    <td>
      <props:checkboxProperty name="${ib.updatePackagesPrerelease}"/>
      <label for="${ib.updatePackagesPrerelease}">Include pre-release packages</label>
      <span class="smallNote">Equivalent to -Prerelease NuGet option</span>
    </td>
  </tr>
  <tr>
    <td>
      <props:checkboxProperty name="${ib.updatePackagesSafeKey}"/>
      <label for="${ib.updatePackagesSafeKey}">Perform safe update</label>
      <span class="smallNote">Equivalent to -Safe NuGet option</span>
    </td>
  </tr>
</l:settingsGroup>
