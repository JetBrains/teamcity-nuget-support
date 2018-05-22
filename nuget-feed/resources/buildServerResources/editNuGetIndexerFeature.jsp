<%@ taglib prefix="props" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="feeds" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.packages.Repository>" />
<jsp:useBean id="cons" class="jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants"/>

<table class="runnerFormTable">
    <tr>
        <th><label for="${cons.feedId}">NuGet Feed: <l:star/></label></th>
        <td>
            <props:selectProperty name="${cons.feedId}" className="longField" enableFilter="true">
                <props:option value="">&lt;Select NuGet feed&gt;</props:option>
                <c:forEach var="feed" items="${feeds}">
                    <props:option value="${feed.projectId}/${feed.name}">
                        <c:out value="${feed.projectId}/${feed.name}"/>
                        <c:if test="${not empty feed.description}">
                            (${feed.description})
                        </c:if>
                    </props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="smallNote">NuGet packages indexing will be performed for all .nupkg files published as a build artifacts.</span>
            <span class="error" id="error_${cons.feedId}"></span>
        </td>
    </tr>
</table>
