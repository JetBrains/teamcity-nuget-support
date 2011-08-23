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
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.pack.PackBean" scope="request"/>


<div class="parameter">
  Path to NuGet.exe: <strong><props:displayValue name="${ib.nuGetPathKey}"/></strong>
</div>


<div class="parameter">
  Specification file: <strong><props:displayValue name="${ib.packSpecFile}"/></strong>
</div>

<div class="parameter">
  Version: <strong><props:displayValue name="${ib.packVersion}"/></strong>
</div>

<div class="parameter">
  Base Directory: <strong><props:displayValue name="${ib.packBaseDirectory}"/></strong>
</div>

<div class="parameter">
  Output Directory: <strong><props:displayValue name="${ib.packOutputDirectory}"/></strong>
</div>

<div class="parameter">
  Exclude files: <props:displayValue name="${ib.packExcludePatterns}" showInPopup="${true}"/>
</div>

<div class="parameter">
  Properties: <props:displayValue name="${ib.packProperties}" showInPopup="${true}"/>
</div>

<div class="parameter">
  Create tool package: <strong><props:displayCheckboxValue name="${ib.packAsTool}"/></strong>
</div>

<div class="parameter">
  Include symbols and sources: <strong><props:displayCheckboxValue name="${ib.packSources}"/></strong>
</div>

<div class="parameter">
  Custom commandline: <strong><props:displayValue name="${ib.packCustomCommandline}" showInPopup="${true}"/></strong>
</div>


