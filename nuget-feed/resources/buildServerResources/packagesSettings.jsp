<%--
  ~ Copyright 2000-2016 JetBrains s.r.o.
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
<jsp:useBean id="repositories" scope="request" type="java.util.Collection<jetbrains.buildServer.nuget.feed.server.tab.ProjectRepository>" />
<jsp:useBean id="repositoryTypes" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.packages.RepositoryType>" />
<jsp:useBean id="project" scope="request" type="jetbrains.buildServer.serverSide.SProject" />
<jsp:useBean id="publicKey" scope="request" type="java.lang.String"/>
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.feed.server.tab.PackagesConstants"/>

<c:set var="numberOfRepositoryTypes" value="${fn:length(repositoryTypes)}"/>
<c:set var="canEdit" value="${afn:permissionGrantedForProject(project, 'EDIT_PROJECT') and not project.readOnly}"/>

<bs:refreshable containerId="packages" pageUrl="${statusRefreshUrl}">

    <div class="section noMargin">
        <h2 style="border: none">
            <c:choose>
                <c:when test="${numberOfRepositoryTypes gt 1}">
                    Packages
                </c:when>
                <c:otherwise>
                    NuGet Feed
                </c:otherwise>
            </c:choose>
        </h2>
        <bs:smallNote>
            On this page you can configure NuGet feed which could be used to publish packages by builds of this project and its subprojects.
        </bs:smallNote>
        <p style="margin-top: 0">
            <c:if test="${canEdit}">
                <c:forEach var="repoType" items="${repositoryTypes}">
                  <c:set var="projectExternalId" value="${fn:escapeXml(project.externalId)}" />
                  <c:set var="repoTypeType" value="${fn:escapeXml(repoType.type)}" />
                  <c:set var="repoTypeName" value="${fn:escapeXml(repoType.name)}" />

                  <forms:addButton onclick="BS.Packages.AddRepositoryForm.showDialog('${projectExternalId}', '${repoTypeType}', '${repoTypeName}'); return false;">
                    Add new ${repoTypeName}
                  </forms:addButton>
                </c:forEach>
            </c:if>
            <c:if test="${isGuestEnabled}">
                <span class="authEndpoints">
                    Endpoints:
                    <a href="#" class="selected httpAuth" onclick="return BS.Packages.showUrls('httpAuth')">
                        HTTP Basic authentication
                    </a>
                    <span class="separator">|</span>
                    <a href="#" class="guestAuth" onclick="return BS.Packages.showUrls('guestAuth')">
                        Guest authentication
                    </a>
                </span>
            </c:if>
        </p>
        <c:if test="${not empty repositories}">
            <table class="parametersTable packageSources">
                <tr class="header">
                    <th colspan="4">
                        <c:choose>
                            <c:when test="${numberOfRepositoryTypes gt 1}">
                                Configured package repositories
                            </c:when>
                            <c:otherwise>
                                Configured NuGet Feeds
                            </c:otherwise>
                        </c:choose>
                    </th>
                </tr>
                <c:forEach var="entry" items="${repositories}" varStatus="repositoryLoop">
                    <c:set var="entryProjectExternalId" value="${fn:escapeXml(entry.project.externalId)}" />
                    <c:set var="entryProjectName" value="${fn:escapeXml(entry.project.name)}" />
                    <c:set var="entryRepositoryTypeType" value="${fn:escapeXml(entry.repository.type.type)}" />
                    <c:set var="entryRepositoryTypeName" value="${fn:escapeXml(entry.repository.type.name)}" />
                    <c:set var="entryRepositoryName" value="${fn:escapeXml(entry.repository.name)}" />
                    <c:set var="entryUsagesCount" value="${fn:escapeXml(entry.usagesCount)}" />
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${not empty entry.repository.description}">
                                    <c:out value="${entry.repository.description}" />
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${entry.repository.name}" />
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${numberOfRepositoryTypes gt 1}">
                                (${entryRepositoryTypeName})
                            </c:if>
                        </td>
                        <td class="details">
                            <c:set var="parametersDescription" value="${entry.repository.parametersDescription}"/>
                            <c:if test="${not empty parametersDescription}">
                                <c:out value="${parametersDescription}" />
                                <c:if test="${entryUsagesCount gt 0}">
                                    <span style="float: right">has <c:choose>
                                        <c:when test="${entryUsagesCount ge 100}">
                                            100+
                                        </c:when>
                                        <c:otherwise>
                                            ${entryUsagesCount}
                                        </c:otherwise>
                                    </c:choose> usage<bs:s val="${entryUsagesCount}"/>
                                    </span>
                                </c:if>
                                <br/>
                            </c:if>
                            <ul class="httpAuth">
                                <c:forEach var="url" items="${entry.httpAuthUrls}" varStatus="loop">
                                    <li>
                                        <c:set var="endpointUrlId" value="http-auth-${entryRepositoryTypeType}-${entryRepositoryTypeName}-${loop.index}"/>
                                        <span id="${endpointUrlId}" class="grayNote"><c:out value="${url}" /></span>
                                        <bs:copy2ClipboardLink dataId="${endpointUrlId}"/>
                                    </li>
                                </c:forEach>
                            </ul>
                            <c:if test="${isGuestEnabled}">
                                <ul class="guestAuth" style="display: none">
                                    <c:forEach var="url" items="${entry.guestAuthUrls}" varStatus="loop">
                                        <li>
                                            <c:set var="endpointUrlId" value="guest-auth-${entryRepositoryTypeType}-${entryRepositoryTypeName}-${loop.index}"/>
                                            <span id="${endpointUrlId}" class="grayNote"><c:out value="${url}" /></span>
                                            <bs:copy2ClipboardLink dataId="${endpointUrlId}"/>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:if>
                        </td>
                        <td class="edit">
                            <c:if test="${not project.readOnly and canEdit}">
                                <p style="margin-top: 0">
                                    <a href="" onclick="BS.Packages.AddRepositoryForm.showDialog('${entryProjectExternalId}', '${entryRepositoryTypeType}', '${entryRepositoryTypeName}', '${entryRepositoryName}', ${entryUsagesCount}); return false;">Edit</a>
                                </p>
                            </c:if>
                        </td>
                        <td class="edit">
                            <c:if test="${not project.readOnly and canEdit}">
                                <p style="margin-top: 0">
                                    <a href="" onclick="BS.Packages.deleteRepository('${entryProjectExternalId}', '${entryProjectName}', '${entryRepositoryTypeType}', '${entryRepositoryTypeName}', '${entryRepositoryName}', ${entryUsagesCount}); return false;">Delete</a>
                                </p>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <div class="grayNote"><b>HTTP Basic authentication</b>: Lists all packages available for the currently authenticated user (user should have view project permission).</div>
            <c:if test="${isGuestEnabled}">
                <div class="grayNote"><b>Guest authentication</b>: Lists all packages from builds available for the guest<bs:help file="Guest+User"/> user.</div>
            </c:if>
        </c:if>
    </div>

</bs:refreshable>

<bs:modalDialog formId="newRepositoryForm"
                title="Create NuGet Feed"
                action="#"
                closeCommand="BS.Packages.AddRepositoryForm.close();"
                saveCommand="BS.Packages.AddRepositoryForm.submit(); return false;">

    <div id="newRepositoryDiv"></div>
    <div id="usagesNoteDiv"></div>
    <input type="hidden" name="publicKey" value="${publicKey}"/>

    <div class="popupSaveButtonsBlock">
        <forms:submit label="Save" onclick="BS.Packages.AddRepositoryForm.submit(); return false;"/>
        <forms:cancel onclick="BS.Packages.AddRepositoryForm.cancelDialog()" showdiscardchangesmessage="false"/>
        <forms:saving id="newRepositoryProgress"/>
    </div>
</bs:modalDialog>

<script>
    BS.Packages.setPageUrl('${settingsPostUrl}');
</script>
