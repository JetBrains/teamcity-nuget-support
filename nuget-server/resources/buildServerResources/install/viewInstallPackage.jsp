

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.install.InstallBean" scope="request"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
  Path to NuGet.exe: <jsp:include page="../tool/runnerSettings.html?name=${ib.nuGetPathKey}&class=longField&view=1"/>
</div>
<div class="parameter">
  Package Sources: <strong><props:displayValue name="${ib.nuGetSourcesKey}"
                                               emptyValue="Use nuget default package source"/></strong>
</div>
<div class="parameter">
  Path to .sln: <strong><props:displayValue name="${ib.solutionPathKey}"/></strong>
</div>

<div class="parameter">
  Exclude Version: <strong><props:displayCheckboxValue name="${ib.excludeVersionKey}"/></strong>
</div>
<div class="parameter">
  Restore Mode:
  <strong>
    <c:set var="restoreMode" value="${propertiesBean.properties[ib.restoreCommandModeKey]}"/>
    <c:choose>
      <c:when test="${empty restoreMode or (restoreMode eq ib.restoreCommandModeInstallValue)}">Install missing packages</c:when>
      <c:when test="${restoreMode eq ib.restoreCommandModeRestoreValue}">Restore packages (requires NuGet 2.7+)</c:when>
      <c:otherwise>Uknnown restore mode</c:otherwise>
    </c:choose>
  </strong>
</div>
<div class="parameter">
  Use local machine packages cache:
  <strong>
    <props:displayCheckboxValue name="${ib.noCacheKey}" checkedValue="NO" uncheckedValue="YES"/>
  </strong>
</div>

<div class="parameter">
  Restore / install command custom command line: <strong><props:displayValue name="${ib.restoreCustomCommandline}" showInPopup="${true}"/></strong>
</div>

<div class="parameter">
  Update packages:
  <strong>
    <props:displayCheckboxValue name="${ib.updatePackagesKey}"/>
  </strong>
</div>
<div class="parameter">
  Update mode:
  <strong>
    <c:set var="updateMode" value="${propertiesBean.properties[ib.updateModeKey]}"/>
    <c:choose>
      <c:when test="${updateMode eq ib.updatePerConfigValue}">Update via packages.config file</c:when>
      <c:when test="${updateMode eq ib.updatePerSolutionValue}">Update via solution file</c:when>
      <c:otherwise>Update via solution file</c:otherwise>
    </c:choose>
  </strong>
</div>
<div class="parameter">
  Use safe packages update:
  <strong>
    <props:displayCheckboxValue name="${ib.updatePackagesSafeKey}"/>
  </strong>
</div>
<div class="parameter">
  Include PreRelease packages:
  <strong>
    <props:displayCheckboxValue name="${ib.updatePackagesPrerelease}"/>
  </strong>
</div>

<div class="parameter">
  Update command custom command line: <strong><props:displayValue name="${ib.updateCustomCommandline}" showInPopup="${true}"/></strong>
</div>
