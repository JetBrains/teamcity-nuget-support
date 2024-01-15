

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="ab" class="jetbrains.buildServer.nuget.server.runner.auth.AuthBean" scope="request"/>

<tr>
  <td colspan="2">
    <em>Enables interacting with feeds which require authentication.</em><bs:help file="NuGet+Feed+Credentials"/>
  </td>
</tr>

<tr>
  <th><label for="${ab.feedKey}">Feed URL:<l:star/></label></th>
  <td>
    <props:textProperty name="${ab.feedKey}" className="longField"/>
    <span class="smallNote">Specify a feed URL which credentials will be used in the build</span>
    <span class="error" id="error_${ab.feedKey}"></span>
  </td>
</tr>

<l:settingsGroup title="Credentials">
  <tr>
    <th><label for="${ab.usernameKey}">Username:<l:star/></label></th>
    <td>
      <props:textProperty name="${ab.usernameKey}" className="longField"/>
      <span class="error" id="error_${ab.usernameKey}"></span>
    </td>
  </tr>
  <tr>
    <th><label for="${ab.passwordKey}">Password:<l:star/></label></th>
    <td>
      <props:passwordProperty name="${ab.passwordKey}" className="longField"/>
      <span class="error" id="error_${ab.passwordKey}"></span>
    </td>
  </tr>
</l:settingsGroup>

<tr>
  <td colspan="2">Only NuGet 2.0+ is supported</td>
</tr>
