<%--
  ~ Copyright 2000-2013 JetBrains s.r.o.
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
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.auth.AuthBean" scope="request"/>

<tr>
  <td colspan="2">Specify NuGet feed credentials</td>
</tr>

<tr>
  <th><label>Feed URI:<l:star/></label></th>
  <td>
    <props:textProperty name="feedUri" className="longField"/>
    <span class="smallNote">Specify NuGet feed URL in the same way as you use in build</span>
  </td>
</tr>

<l:settingsGroup title="Credentials">
  <tr>
    <th><label>Username:<l:star/></label></th>
    <td>
      <props:textProperty name="username" className="longField"/>
    </td>
  </tr>
  <tr>
    <th><label>Password:<l:star/></label></th>
    <td>
      <props:textProperty name="password" className="longField"/>
    </td>
  </tr>
</l:settingsGroup>

<tr>
  <td colspan="2">Only NuGet 2.0+ are supported</td>
</tr>
