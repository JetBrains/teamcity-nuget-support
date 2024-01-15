

<%@ include file="/include-internal.jsp" %>
<jsp:useBean id="nugetPrivateUrl" type="java.lang.String"  scope="request"/>
<jsp:useBean id="nugetPublicUrl" type="java.lang.String"  scope="request"/>
<jsp:useBean id="imagesUrl" type="java.lang.String"  scope="request"/>

<c:set var="nugetPrivatePath"><c:url value="${nugetPrivateUrl}"/></c:set>
<c:set var="nugetPublicPath"><c:url value="${nugetPublicUrl}"/></c:set>

<p class="toolTitle" style="background: url('<c:url value="${imagesUrl}/nuget.png"/>') no-repeat">NuGet Feeds</p>

<a showdiscardchangesmessage="false"
   title="NuGet Feed Url"
   href="${nugetPrivatePath}">NuGet feed url</a>
