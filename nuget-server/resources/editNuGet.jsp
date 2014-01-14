<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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

<tr>
  <th>NuGet .nuspec path:</th>
  <td><props:textProperty name="nuget.package.spec.path" className="longField"/>
    <span class="smallNote">Specify path to NuGet specifications file (.nuspec)</span>
  </td>
</tr>

<tr>
  <th>Upload:</th>
  <td>
    <props:selectProperty name="nuget.upload.mode">
      <props:option value="">No Upload</props:option>
      <props:option value="artifact">Publish as build artifact</props:option>
      <props:option value="local">Publish to local package repository</props:option>
      <props:option value="nuget.org">Upload to NuGet.org</props:option>
      <props:option value="nuget.org">Upload to custom server</props:option>
    </props:selectProperty>
    <span class="smallNote">Specify way to publish NuGet package</span>
  </td>
</tr>
