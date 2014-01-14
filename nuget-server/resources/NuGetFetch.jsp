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

<%--
<l:settingsGroup title="Install packages">
<tr>
  <th>Install packages:</th>
  <td>
    <props:multilineProperty name="nuget.packages" linkTitle="Expand" cols="70" rows="5"/>
    <span class="smallNote">Specify packages to install before the build</span>
  </td>
</tr>

<tr>
  <th>Destination Path:</th>
  <td>
    <props:textProperty name="nuget.destination.path" className="longField"/>
    <span class="smallNote">Specify folder where to install NuGet packages</span>
  </td>
</tr>
</l:settingsGroup>
--%>

<l:settingsGroup title="Update packages">
<tr>
  <th>Packages repository path:</th>
  <td>
    <props:textProperty name="nuget.repositofies.config" className="longField"/>
    <span class="smallNote">Specify path to NuGet repositories.config file under solution</span>
  </td>
</tr>
</l:settingsGroup>