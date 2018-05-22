<%@include file="/include-internal.jsp" %>
<%--@elvariable id="repositoryType" type="jetbrains.buildServer.serverSide.packages.RepositoryType"--%>
<jsp:useBean id="cons" class="jetbrains.buildServer.nuget.feed.server.tab.PackagesConstants"/>

<table class="runnerFormTable">
    <tr>
        <th><label for="${cons.name}">Name: <l:star/></label></th>
        <td>
            <props:textProperty name="${cons.name}" className="longField"/>
            <span class="error" id="error_${cons.name}"></span>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.description}">Description:</label></th>
        <td>
            <props:textProperty name="${cons.description}" className="longField"/>
            <span class="smallNote">The optional description for NuGet feed.</span>
            <span class="error" id="error_${cons.description}"></span>
        </td>
    </tr>
    <tr>
        <td colspan="2" class="noBorder">
            <props:hiddenProperty name="${cons.type}" id="repositoryType" />
            <span class="error" id="error_${cons.type}"></span>
        </td>
    </tr>
    <jsp:include page="${repositoryType.editParametersUrl}"/>
</table>
