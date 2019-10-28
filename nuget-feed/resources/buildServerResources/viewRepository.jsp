<%--
  ~ Copyright 2000-2019 JetBrains s.r.o.
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ taglib prefix="intprop" uri="/WEB-INF/functions/intprop" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="isGuestEnabled" type="java.lang.Boolean" scope="request"/>

<jsp:useBean id="statusRefreshUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="settingsPostUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="repositoryTypes" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.packages.RepositoryType>" />
<jsp:useBean id="publicKey" scope="request" type="java.lang.String"/>
<jsp:useBean id="reactUiScriptUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.feed.server.tab.PackagesConstants"/>

<c:set var="numberOfRepositoryTypes" value="${fn:length(repositoryTypes)}"/>

<bs:refreshable containerId="packages" pageUrl="${statusRefreshUrl}">

    <span class="section noMargin">
      View Feed
    </span>

</bs:refreshable>

<script>
    BS.Packages.setPageUrl('${settingsPostUrl}');
</script>

<div id="rootNugetFeedReact"/>
<script src="${reactUiScriptUrl}"/>
