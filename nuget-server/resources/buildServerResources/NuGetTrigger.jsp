<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>




<tr>
  <th>Packages repository path:</th>
  <td>
    <props:textProperty name="nuget.repositofies.config" className="longField"/>
    <span class="smallNote">Specify path to repositories.config to list packages to monitor</span>
  </td>
</tr>
