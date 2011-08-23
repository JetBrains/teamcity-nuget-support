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
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.install.InstallBean" scope="request"/>

<div class="parameter">
  Path to NuGet.exe: <strong><props:displayValue name="${ib.nuGetPathKey}"/></strong>
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
  Update packages:
  <strong>
    <props:displayCheckboxValue name="${ib.updatePackagesKey}"/>
  </strong>
</div>
<div class="parameter">
  Use safe packages update:
  <strong>
    <props:displayCheckboxValue name="${ib.updatePackagesSafeKey}"/>
  </strong>
</div>
