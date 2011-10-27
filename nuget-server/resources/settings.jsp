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
<jsp:useBean id="nuget_teamcity_include_controllers" scope="request" type="java.util.Collection<java.lang.String>"/>

<c:forEach items="${nuget_teamcity_include_controllers}" var="nugetSettingsPage">
  <div style="padding-bottom: 3em;">
    <!-- start of NuGet Settings page: <c:out value="${nugetSettingsPage}"/> -->
    <jsp:include page="${nugetSettingsPage}"/>
    <!-- end of NuGet Settings page: <c:out value="${nugetSettingsPage}"/> -->
  </div>
</c:forEach>


