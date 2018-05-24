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
<%@ page import="jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ taglib prefix="intprop" uri="/WEB-INF/functions/intprop" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="isGuestEnabled" type="java.lang.Boolean" scope="request"/>

<jsp:useBean id="statusRefreshUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="settingsPostUrl" scope="request" type="java.lang.String"/>
<jsp:useBean id="repositories" scope="request" type="java.util.Collection<jetbrains.buildServer.nuget.feed.server.tab.ProjectRepository>" />
<jsp:useBean id="repositoryTypes" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.packages.RepositoryType>" />
<jsp:useBean id="hasDefaultFeedIndexing" scope="request" type="java.lang.Boolean" />
<jsp:useBean id="project" scope="request" type="jetbrains.buildServer.serverSide.SProject" />
<jsp:useBean id="publicKey" scope="request" type="java.lang.String"/>
<jsp:useBean id="fb" class="jetbrains.buildServer.nuget.feed.server.tab.PackagesConstants"/>

<%--@elvariable id="defaultFeed" type="jetbrains.buildServer.nuget.feed.server.tab.ProjectRepository"--%>

<c:set var="numberOfRepositoryTypes" value="${fn:length(repositoryTypes)}"/>
<c:set var="useDefaultFeedProperty" value="<%= NuGetFeedConstants.PROP_NUGET_FEED_USE_DEFAULT%>"/>
<c:set var="useDefaultFeed" value="${intprop:getBooleanOrTrue(useDefaultFeedProperty)}"/>
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
            <c:choose>
                <c:when test="${useDefaultFeed}">
                    NuGet project feed is
                    <c:choose>
                        <c:when test="${not empty defaultFeed}">
                            <strong>enabled</strong> <c:if test="${canEdit}"><input
                                type="button" class="btn btn_mini" value="Delete"
                                onclick="BS.Packages.deleteRepository('${defaultFeed.project.externalId}', '${defaultFeed.project.name}', '${defaultFeed.repository.type.type}', '${defaultFeed.repository.type.name}', '${defaultFeed.repository.name}'); return false;"/></c:if>
                        </c:when>
                        <c:otherwise>
                            <strong>disabled</strong> <c:if test="${canEdit}"><input
                                type="button" class="btn btn_mini" value="Enable"
                                onclick="BS.Packages.addRepository('${project.externalId}', 'nuget', 'default'); return false;"/></c:if>
                        </c:otherwise>
                    </c:choose>
                    <span><%--used for loading icon--%></span>
                </c:when>
                <c:otherwise>
                    <c:if test="${canEdit}">
                        <c:forEach var="repoType" items="${repositoryTypes}">
                            <forms:addButton onclick="BS.Packages.AddRepositoryForm.showDialog('${project.externalId}', '${repoType.type}', '${repoType.name}'); return false;">
                                Add ${repoType.name}
                            </forms:addButton>
                        </c:forEach>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </p>
        <c:if test="${not empty repositories}">
            <table class="parametersTable">
                    <tr class="header">
                        <th colspan="4">
                            <c:choose>
                                <c:when test="${useDefaultFeed}">
                                    Feed endpoints
                                </c:when>
                                <c:otherwise>
                                    Configured package repositories
                                </c:otherwise>
                            </c:choose>
                        </th>
                    </tr>
                <c:forEach var="entry" items="${repositories}">
                    <tr>
                        <c:if test="${not useDefaultFeed}">
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
                                    (${entry.repository.type.name})
                                </c:if>
                            </td>
                        </c:if>
                        <td>
                            HTTP Basic authentication:
                            <ul>
                                <c:forEach var="url" items="${entry.httpAuthUrls}" varStatus="loop">
                                    <li>
                                        <c:set var="endpointUrlId" value="http-auth-${entry.repository.type.type}-${entry.repository.name}-${loop.index}"/>
                                        <span id="${endpointUrlId}" class="grayNote"><c:out value="${url}" /></span>
                                        <bs:copy2ClipboardLink dataId="${endpointUrlId}"/>
                                    </li>
                                </c:forEach>
                            </ul>
                            <c:if test="${isGuestEnabled}">
                                Guest authentication:
                                <ul>
                                    <c:forEach var="url" items="${entry.guestAuthUrls}" varStatus="loop">
                                        <li>
                                            <c:set var="endpointUrlId" value="guest-auth-${entry.repository.type.type}-${entry.repository.name}-${loop.index}"/>
                                            <span id="${endpointUrlId}" class="grayNote"><c:out value="${url}" /></span>
                                            <bs:copy2ClipboardLink dataId="${endpointUrlId}"/>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:if>
                        </td>
                        <c:if test="${not useDefaultFeed}">
                            <td class="edit">
                                <c:if test="${not project.readOnly and canEdit}">
                                    <p style="margin-top: 0">
                                        <a href="" onclick="BS.Packages.AddRepositoryForm.showDialog('${entry.project.externalId}', '${entry.repository.type.type}', '${entry.repository.type.name}', '${entry.repository.name}'); return false;">Edit</a>
                                    </p>
                                </c:if>
                            </td>
                            <td class="edit">
                                <c:if test="${not project.readOnly and canEdit}">
                                    <p style="margin-top: 0">
                                        <a href="" onclick="BS.Packages.deleteRepository('${entry.project.externalId}', '${entry.project.name}', '${entry.repository.type.type}', '${entry.repository.type.name}', '${entry.repository.name}'); return false;">Delete</a>
                                    </p>
                                </c:if>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
            </table>

            <div class="grayNote"><b>HTTP Basic authentication</b>: Lists all packages available for the currently authenticated user (user should have view project permission).</div>
            <c:if test="${isGuestEnabled}">
                <div class="grayNote"><b>Guest authentication</b>: Lists all packages from builds available for the guest<bs:help file="Guest+User"/> user.</div>
            </c:if>
        </c:if>
    </div>


    <c:if test="${useDefaultFeed and not empty defaultFeed}">
        <h2 style="border: none">Automatic packages indexing</h2>
        <bs:smallNote>
            When this setting is enabled, all NuGet packages published by builds of this project and its subprojects will be available in the NuGet feed.
            To publish NuGet packages for builds of selected configurations, add the NuGet packages indexer<bs:help file="NuGet"/> build feature to them.
        </bs:smallNote>
        <c:if test="${project.externalId eq '_Root'}">
            <div class="attentionComment">
                Due to performance reasons it is recommended to disable global packages indexing and use
                NuGet packages indexer build feature only in required build configurations.
            </div>
        </c:if>
        <div data-url="${settingsPostUrl}">
            Automatic packages indexing is
            <c:choose>
                <c:when test="${hasDefaultFeedIndexing}">
                    <strong>enabled</strong> <c:if test="${canEdit}"><input
                        type="button" class="btn btn_mini" value="Disable"
                        onclick="BS.NuGet.FeedServer.toggleFeedIndexing('${defaultFeed.project.externalId}', '${defaultFeed.repository.type.type}', '${defaultFeed.repository.name}', false, this); return false;"/></c:if>
                </c:when>
                <c:otherwise>
                    <strong>disabled</strong> <c:if test="${canEdit}"><input
                        type="button" class="btn btn_mini" value="Enable"
                        onclick="BS.NuGet.FeedServer.toggleFeedIndexing('${defaultFeed.project.externalId}', '${defaultFeed.repository.type.type}', '${defaultFeed.repository.name}', true, this); return false;"/></c:if>
                </c:otherwise>
            </c:choose>
            <span><%--used for loading icon--%></span>
        </div>
    </c:if>

</bs:refreshable>

<bs:modalDialog formId="newRepositoryForm"
                title="Create NuGet Feed"
                action="#"
                closeCommand="BS.Packages.AddRepositoryForm.close();"
                saveCommand="BS.Packages.AddRepositoryForm.submit(); return false;">

    <div id="newRepositoryDiv"></div>
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
