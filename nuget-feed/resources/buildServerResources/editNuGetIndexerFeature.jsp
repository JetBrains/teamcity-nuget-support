<%@ taglib prefix="props" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="feeds" scope="request" type="java.util.Collection<jetbrains.buildServer.nuget.feed.server.packages.ProjectFeed>" />
<jsp:useBean id="cons" class="jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants"/>

<table class="runnerFormTable">
    <tr>
        <th><label for="${cons.feed}">NuGet Feed: <l:star/></label></th>
        <td>
            <props:selectProperty name="${cons.feed}" className="longField" enableFilter="true">
                <props:option value="">-- Select NuGet feed --</props:option>
                <c:forEach var="feed" items="${feeds}">
                    <props:option value="${feed.project.externalId}/${feed.repository.name}">
                        <c:choose>
                            <c:when test="${feed.repository.name ne 'default'}">
                                <c:out value="${feed.project.name}/${feed.repository.name}"/>
                                <c:if test="${not empty feed.repository.description}">
                                    (${feed.repository.description})
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${feed.project.name}"/>
                            </c:otherwise>
                        </c:choose>
                    </props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="smallNote">NuGet packages indexing will be performed for all .nupkg files published as a build artifacts.</span>
            <span class="error" id="error_${cons.feed}"></span>
        </td>
    </tr>
</table>
