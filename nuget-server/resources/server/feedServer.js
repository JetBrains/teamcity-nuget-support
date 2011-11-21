/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
  refreshStatus : function () {
    $('nugetServerStatus').refresh();
  },

  registerStatusRefresh : function () {
    var that = this;
    setTimeout(function () {
      that.refreshStatus();
      that.registerStatusRefresh();
    }, 1000);
  },

  refreshLog : function () {
    $('nugetServerLogs').refresh();
  },

  disableFeedServer : function() {
    BS.NuGet.FeedServer.DisableForm.show();
  },

  enableFeedServer : function() {
    BS.NuGet.FeedServer.EnableForm.show();
  }
};

BS.NuGet.FeedServer.EnableDisableForm = OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractModalDialog, {
  getContainer : function () {
    return $(this.formElement().id + 'Dialog');
  },

  saveForm : function () {
    var that = this;
    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
      onCompleteSave : function () {
        BS.Util.reenableForm(that.formElement());
        BS.NuGet.FeedServer.refreshStatus();
        $('nugetEnableDisable').refresh();
        $('nugetServerStatus').refresh();
        that.close();
      }
    }));
    return false;
  },

  show : function() {
    this.showCentered();
  }
}));

BS.NuGet.FeedServer.EnableForm = OO.extend(BS.NuGet.FeedServer.EnableDisableForm, {
  formElement : function () {
    return $('nugetEnableFeed');
  }
});

BS.NuGet.FeedServer.DisableForm = OO.extend(BS.NuGet.FeedServer.EnableDisableForm, {
  formElement : function () {
    return $('nugetDisableFeed');
  }
});

Event.observe(window, "load", function () {
  BS.NuGet.FeedServer.registerStatusRefresh();
});

