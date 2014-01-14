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
<%@ include file="/include-internal.jsp" %>
<jsp:useBean id="installTools" type="jetbrains.buildServer.nuget.server.toolRegistry.tab.InstallToolBean" scope="request"/>

<tr>
  <th>Options:</th>
  <td>
    <forms:checkbox name="setAsDefault" checked="${true}"/>
    <label for="setAsDefault">Set as Default</label>

    <input type="hidden" name="whatToDo" value="${installTools.whatToDo}" />
  </td>
</tr>
