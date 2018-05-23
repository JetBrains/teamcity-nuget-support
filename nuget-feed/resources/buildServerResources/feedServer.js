/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

if (!BS) BS = {};
if (!BS.NuGet) BS.NuGet = {};

BS.NuGet.FeedServer = {
  _request : function(projectId, type, name, enabled, el) {
    var url = $j(el).closest("div").data("url");

    $j(el).closest("div").find("span:last").html(BS.loadingIcon);
    $j(el).prop("disabled", "true");

    BS.ajaxRequest(url, {
      method : "POST",
      parameters : {
        'action': 'nugetFeedIndexing',
        'projectId' : projectId,
        'type' : type,
        'name' : name,
        'enabled' : enabled
      },
      onComplete : function() {
        $j(el).closest("div").find("span:last").html('');
        BS.reload(true);
      }
    });
  },

  toggleFeedIndexing : function(projectId, type, name, enabled, el) {
    BS.NuGet.FeedServer._request(projectId, type, name, enabled, el);
  }
};
