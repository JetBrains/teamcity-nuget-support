<%--
  ~ Copyright 2000-2011 JetBrains s.r.o.
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
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>


<h2 class="noBorder">TeamCity as NuGet Feed</h2>
<div style="width: 50em;">
<p>In this section you may select if you like to make TeamCity be a NuGet feed.</p>

  <table class="runnerFormTable">
    <tr>
      <th>Enable NuGet Server:</th>
      <td>
        <props:checkboxProperty name="enabled"/> Enabled NuGet Server
        <span class="smallNote">Enabled or disabled nuget feed server running inside TeamCity</span>
      </td>
    </tr>

    <tr>
      <th>
        TeamCity Url:
      </th>
      <td>
        <props:textProperty name="teamcityurl" className="longField"/>
        <span class="smallNote">
          NuGet server is a .NET process running under TeamCity. It's required to make the
          process connect back to TeamCity server. Specify here TeamCity url that can be use to
          connect to it from the same machine.
        </span>
      </td>
    </tr>
  </table>

  <div class="saveButtonsBlock" style="border: none;">
    <input class="submitButton" type="submit" value="Save">
    <input type="hidden" id="submitSettings" name="submitSettings" value="store"/>
    <forms:saving/>
  </div>

  <div class="clr"></div>


  <div>
    Failed to make NuGet Server communicate with TeamCity. See logs for details:
    <pre>
      Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum
      Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum
      Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum
      Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum Loren ipsum
    </pre>
  </div>

</div>



