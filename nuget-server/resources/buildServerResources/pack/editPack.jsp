

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.pack.PackBean" scope="request"/>

<jsp:include page="/tools/editToolUsage.html?toolType=${ib.nugetToolTypeName}&versionParameterName=${ib.nuGetPathKey}&class=longField"/>

<script type="text/javascript">
  appendSpecificationFile = function(specFile) {
    var textarea = $j(BS.Util.escapeId('${ib.packSpecFile}'));
    var val = textarea.text();
    if (val.length > 0) {
      var lines = val.split("\n");
      lines.push(specFile);
      textarea.text(lines.join("\n"));
    } else {
      textarea.text(specFile);
    }
  };
</script>

<l:settingsGroup title="Package Parameters">
  <tr>
    <th><label for="${ib.packSpecFile}">Specification files</label><l:star/>:</th>
    <td>
      <props:multilineProperty name="${ib.packSpecFile}" linkTitle="Specification or project files" cols="60" rows="5" expanded="${true}">
      <jsp:attribute name="afterTextField"><bs:vcsTree callback="appendSpecificationFile" treeId="${ib.packSpecFile}"/></jsp:attribute>
      </props:multilineProperty>
      <span class="smallNote">Specify paths to .nuspec files and/or to Visual Studio project files (i.e. .csproj or .vbproj). MSBuild-style wildcards are supported</span>
      <span id="error_${ib.packSpecFile}" class="error"></span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <th class="noBorder"></th>
    <td>
      <props:checkboxProperty name="${ib.packPreferProject}"/>
      <label for="${ib.packPreferProject}">Prefer project files to .nuspec</label>
      <span class="smallNote">Use the project file (if exists, i.e. .csproj or .vbproj) for every matched .nuspec file</span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${ib.packVersion}">Version</label>:</th>
    <td>
      <props:textProperty name="${ib.packVersion}" className="longField"/>
      <span class="smallNote">Overrides the version number from the nuspec file.</span>
      <span id="error_${ib.packVersion}" class="error"></span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${ib.packBaseDirectoryMode}">Base directory</label>:</th>
    <td>
      <props:selectProperty name="${ib.packBaseDirectoryMode}" onchange="BS.NuGet.PackRunnerSettings.onBaseDirModeChange();">
        <c:forEach var="it" items="${ib.packBaseDirectoryModes}">
          <props:option value="${it.value}"><c:out value="${it.description}"/></props:option>
        </c:forEach>
      </props:selectProperty>
      <c:forEach var="it" items="${ib.packBaseDirectoryModes}">
        <c:choose>
          <c:when test="${it.showBaseDirectorySelector}">
            <props:hiddenProperty name="${it.value}-showBaseDirectorySelector"/>
            <div id="content-${it.value}" class="packBaseDirectoryModeContent" style="padding-top: 8px">
              <props:textProperty name="${ib.packBaseDirectory}" className="longField">
              <jsp:attribute name="afterTextField"><bs:vcsTree fieldId="${ib.packBaseDirectory}" treeId="${ib.packBaseDirectory}"/></jsp:attribute>
              </props:textProperty>
              <span class="smallNote"><c:out value="${it.details}"/></span>
              <span id="error_${ib.packBaseDirectory}" class="error"></span>
            </div>
          </c:when>
          <c:otherwise>
            <div id="content-${it.value}" class="packBaseDirectoryModeContent">
              <span class="smallNote"><c:out value="${it.details}"/></span>
            </div>
          </c:otherwise>
        </c:choose>
      </c:forEach>
    </td>
  </tr>

</l:settingsGroup>

<l:settingsGroup title="Output">
  <tr>
    <th rowspan="3"><label for="${ib.packOutputDirectory}">Output directory</label><l:star/>:</th>
    <td>
      <props:textProperty name="${ib.packOutputDirectory}" className="longField"/>
      <span class="smallNote">The path to the output directory for generated NuGet packages. See also <em>NuGet Publish</em> build runner</span>
      <span id="error_${ib.packOutputDirectory}" class="error"></span>
    </td>
  </tr>
  <tr class="advancedSetting">
    <td>
      <props:checkboxProperty name="${ib.packOutputClear}"/><label for="${ib.packOutputClear}">Clean output directory</label>
    </td>
  </tr>
  <tr>
    <td>
      <props:checkboxProperty name="${ib.packAsArtifact}"/><label for="${ib.packAsArtifact}">Publish created packages to build artifacts</label>
      <span class="smallNote">Created packages will be published to the root of artifacts</span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Additionals Parameters" className="advancedSetting">

  <tr class="advancedSetting">
    <th><label for="${ib.packExcludePatterns}">Exclude files: </label></th>
    <td>
      <props:multilineProperty name="${ib.packExcludePatterns}" linkTitle="Exclude files" cols="60" rows="5" />
      <span class="smallNote">Exclude files when creating a package. Equavalent to the NuGet.exe <em>-Exclude</em> argument</span>
      <span id="error_${ib.packExcludePatterns}" class="error"></span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${ib.packProperties}">Properties:</label></th>
    <td>
      <props:multilineProperty name="${ib.packProperties}" linkTitle="Properties" cols="60" rows="5" />
      <span class="smallNote">
        A semicolon or a newline-separated list of package creation properties (i.e. key=value) to pass to the NuGet.exe
      </span>
      <span id="error_${ib.packProperties}" class="error"></span>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th rowspan="2">Options:</th>
    <td>
      <props:checkboxProperty name="${ib.packAsTool}"/>
      <label for="${ib.packAsTool}">Create <strong>tool</strong> package</label>
    </td>
  </tr>
  <tr class="advancedSetting">
    <td>
      <props:checkboxProperty name="${ib.packSources}"/>
      <label for="${ib.packSources}">Include sources and symbols</label>
    </td>
  </tr>

  <tr class="advancedSetting">
    <th><label for="${ib.packCustomCommandline}">Command line parameters:</label></th>
    <td>
      <props:textProperty name="${ib.packCustomCommandline}" className="longField" expandable="true"/>
      <span class="smallNote">Enter additional parameters to use when calling <a href="https://docs.microsoft.com/en-us/nuget/tools/cli-ref-pack">nuget pack</a> command</span>
      <span id="error_${ib.packCustomCommandline}" class="error"></span>
    </td>
  </tr>

</l:settingsGroup>

<script type="text/javascript">
  if (!BS) BS = {};
  if (!BS.NuGet) BS.NuGet = {};
  BS.NuGet.PackRunnerSettings = {
    onBaseDirModeChange : function() {
      var selected = $('${ib.packBaseDirectoryMode}').value;
      $j('div .packBaseDirectoryModeContent').hide();
      if(!($(selected + '-showBaseDirectorySelector'))){
        $('${ib.packBaseDirectory}').value = '';
      }
      BS.Util.show('content-' + selected);
    }
  };
  BS.NuGet.PackRunnerSettings.onBaseDirModeChange();
</script>
