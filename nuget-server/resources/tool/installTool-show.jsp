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
<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="available"
             type="java.util.Collection<jetbrains.buildServer.nuget.server.toolRegistry.tab.InstallableTool>"
             scope="request"/>
<div id="nugetInstallFormResreshInner">
  <c:choose>
    <c:when test="${fn:length(available) eq 0}">
      No other NuGet.exe Command Line packages available
    </c:when>
    <c:otherwise>
      <h4>Select NuGet.exe Command Line version to install:</h4>
      <table style="padding-left: 5em;">
        <c:forEach var="t" items="${available}">
          <tr>
            <td style="width: 10em; text-align: left;">
              NuGet <c:out value="${t.version}"/>
            </td>
            <td>
              <c:choose>
              <c:when test="${t.allreadyInstalled}">
              <strong>installed</strong>
              </c:when>
              <c:otherwise>
              <input type="button" value="Install"/>
              </c:otherwise>
              </c:choose>
          </tr>
        </c:forEach>
      </table>
    </c:otherwise>
  </c:choose>

  <h4>Upload your own NuGet.exe Command Line:</h4>
  <table>
    <tr>
      <td>NuGet Version<l:star/>:</td>
      <td><props:textProperty name="version"/></td>
    </tr>
    <tr>
      <td>NuGet.exe file<l:star/>:</td>
      <td><input type="file" name="file"></td>
    </tr>
  </table>
  <input type="button" value="Upload"/>

</div>