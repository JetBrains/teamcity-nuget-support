<%--
  ~ Copyright 2000-2014 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

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
