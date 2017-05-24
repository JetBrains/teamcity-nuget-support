<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="includeUrl" class="java.lang.String" scope="request"/>
<jsp:include page="${includeUrl}" />