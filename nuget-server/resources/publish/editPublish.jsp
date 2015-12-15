<%--
  ~ Copyright 2000-2015 JetBrains s.r.o.
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

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="ib" class="jetbrains.buildServer.nuget.server.runner.publish.PublishBean" scope="request"/>

<jsp:include page="../tool/editNugetExeSettings.html?name=${ib.nuGetPathKey}&class=longField"/>

<script type="text/javascript">
  appendPackageToUpload = function(packageFile) {
    var textarea = $j(BS.Util.escapeId('${ib.nuGetPublishFilesKey}'));
    var val = textarea.text();
    if (val.length > 0) {
      var lines = val.split("\n");
      lines.push(packageFile);
      textarea.text(lines.join("\n"));
    } else {
      textarea.text(packageFile);
    }
  };
</script>

<tr>
  <th>Packages<l:star/>:</th>
  <td>
    <props:multilineProperty name="${ib.nuGetPublishFilesKey}" linkTitle="Package files"
                             cols="60" rows="5"
                             expanded="${true}"/>
    <bs:vcsTree callback="appendPackageToUpload" treeId="${ib.nuGetPublishFilesKey}"/>
    <script type="text/javascript">
      BS.Util.hide($('vcsTreeControl_${ib.nuGetPublishFilesKey}'));
    </script>
    <span class="smallNote">A newline-separated list of NuGet package files (.nupkg) to push to the NuGet feed. Wildcards are supported.</span>
    <span class="error" id="error_${ib.nuGetPublishFilesKey}"></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th class="noBorder"></th>
  <td>
    <props:checkboxProperty name="${ib.nuGetPublishCreateOnlyKey}"/>
    <label for="${ib.nuGetPublishCreateOnlyKey}">Only upload packages but do not publish them to feed</label>
    <span class="smallNote">Specifies if packages should be created and uploaded to the TeamCity server but not published to the NuGet feed.</span>
  </td>
</tr>

<tr>
  <th>API key:</th>
  <td>
    <props:passwordProperty name="${ib.apiKey}" className="longField"/>
      <span class="smallNote">
        Specify the API key to access a NuGet packages feed.
      </span>
    <span class="error" id="error_${ib.apiKey}"></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th>Package Source:</th>
  <td>
    <props:textProperty name="${ib.nuGetSourceKey}" className="longField"/>
      <span class="smallNote">
        Specify the NuGet packages feed URL to push packages to. Leave blank to let NuGet decide what package repository to use.
      </span>
    <span class="error" id="error_${ib.nuGetSourceKey}"></span>
  </td>
</tr>

<tr class="advancedSetting">
  <th><label for="${ib.pushCustomCommandline}">Command line parameters:</label></th>
  <td>
    <props:textProperty name="${ib.pushCustomCommandline}" className="longField" expandable="true"/>
    <span class="smallNote">Enter additional parameters to use when calling <a href="http://docs.nuget.org/docs/reference/command-line-reference#push-command">nuget push</a> command</span>
    <span id="error_${ib.pushCustomCommandline}" class="error"></span>
  </td>
</tr>

<script type="text/javascript">
  $j(document).ready(function() {
    //move vcs-tree icon to the left from textarea, after completion icon
    var img = $('vcsTreeControl_${ib.nuGetPublishFilesKey}');
    if (!img) {
      //there is no icon when no vcs roots configured
      return;
    }
    img.remove();
    img.style.position = 'absolute';

    var textarea = $('${ib.nuGetPublishFilesKey}');
    var dim = textarea.getDimensions(),
        layout = textarea.getLayout();

    var xshift = dim.width + 20; // Put next to the completion icon
    var pos = textarea.positionedOffset();

    textarea.parentNode.appendChild(img);
    var x = pos[0] + xshift + layout.get('margin-left');
    var y = pos[1] + 3 + layout.get('margin-top');
    BS.Util.show(img);
    BS.Util.place(img, x, y);
  });
</script>