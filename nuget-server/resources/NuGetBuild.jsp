<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="nugetPackages" scope="request" type="java.util.Map<java.lang.String, java.lang.String>"/>

This is the list of all NuGet packages that were downloaded for the build.

<table class="runnerFormTable" style="width:40em">
  <thead>
    <th>Package Name</th>
    <th>Package Version</th>
  </thead>
<c:forEach var="it" items="${nugetPackages}">
  <tr>
    <td><a href="http://somewhere">${it.key}</a></td>
    <td>${it.value}</td>
  </tr>
</c:forEach>
</table>