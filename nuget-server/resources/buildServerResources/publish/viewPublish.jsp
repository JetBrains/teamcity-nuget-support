

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.publish.PublishBean" scope="request"/>

<div class="parameter">
  Path to NuGet.exe: <jsp:include page="../tool/runnerSettings.html?name=${ib.nuGetPathKey}&class=longField&view=1"/>
</div>

<div class="parameter">
  Package Sources: <strong><props:displayValue name="${ib.nuGetSourceKey}"
                                               emptyValue="Use default source"/></strong>
</div>

<div class="parameter">
  Packages to upload: <strong><props:displayValue name="${ib.nuGetPublishFilesKey}" showInPopup="${true}"/></strong>
</div>
