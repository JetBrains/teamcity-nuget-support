<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="cons" class="jetbrains.buildServer.nuget.feed.server.packages.NuGetRepositoryParams"/>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${cons.indexPackages}" />
        <label for="${cons.indexPackages}">Automatic packages indexing</label>
        <span class="smallNote">
          To index all .nupkg files published as build artifacts in this project and its subprojects, enable this feature.<br/>
          To index packages published by selected build configurations only,
          add the NuGet packages indexer<bs:help file="NuGet+Packages+Indexer"/> build feature to these build configurations.
        </span>
    </td>
</tr>
