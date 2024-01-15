<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>



<%--
<l:settingsGroup title="Install packages">
<tr>
  <th>Install packages:</th>
  <td>
    <props:multilineProperty name="nuget.packages" linkTitle="Expand" cols="70" rows="5"/>
    <span class="smallNote">Specify packages to install before the build</span>
  </td>
</tr>

<tr>
  <th>Destination Path:</th>
  <td>
    <props:textProperty name="nuget.destination.path" className="longField"/>
    <span class="smallNote">Specify folder where to install NuGet packages</span>
  </td>
</tr>
</l:settingsGroup>
--%>

<l:settingsGroup title="Update packages">
<tr>
  <th>Packages repository path:</th>
  <td>
    <props:textProperty name="nuget.repositofies.config" className="longField"/>
    <span class="smallNote">Specify path to NuGet repositories.config file under solution</span>
  </td>
</tr>
</l:settingsGroup>
