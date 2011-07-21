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
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.publish.PublishBean" scope="request"/>

<l:settingsGroup title="NuGet settings">
  <tr>
    <th>Path to NuGet.exe<l:star/></th>
    <td>
      <props:textProperty name="${ib.nuGetPathKey}" className="longField"/>
      <span class="smallNote">Specify path to NuGet.exe</span>
      <span class="error" id="error_${ib.nuGetPathKey}"></span>
    </td>
  </tr>
  <tr>
    <th>Package Sources</th>
    <td>
      <props:textProperty name="${ib.nuGetSourceKey}" className="longField"/>
      <span class="smallNote">
        Specify NuGet package sources to push package.
        Leave blank to let NuGet decided what package repository to use (nuget.org by default).
      </span>
      <span class="error" id="error_${ib.nuGetSourceKey}"></span>
    </td>
  </tr>
  <tr>
    <th>API key:<l:star/></th>
    <td>
      <props:textProperty name="${ib.apiKey}" className="longField"/>
      <span class="smallNote">
        Specify API key to access NuGet source.
      </span>
      <span class="error" id="error_${ib.apiKey}"></span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Packages">
  <tr>
    <th>Packages to uploads:<l:star/></th>
    <td>
      <props:multilineProperty name="${ib.nuGetPublishFilesKey}" linkTitle="Packages files"
                               cols="60" rows="5"
                               expanded="${true}"/>
      <span>Specify NuGet package files to push to NuGet Feed. Each file on new line. Wildcards are supported</span>
      <span class="error" id="error_${ib.nuGetPublishFilesKey}"></span>
    </td>
  </tr>
  
  <tr>
    <th>Options:</th>
    <td>
      <props:checkboxProperty name="${ib.nuGetPublishCreateOnlyKey}"/>
      Only upload package but do not pusblish it to feed.
      <span class="smallNote">
        Specifies if the package should be created and uploaded to the server but not published to the server.
      </span>
    </td>
  </tr>
</l:settingsGroup>