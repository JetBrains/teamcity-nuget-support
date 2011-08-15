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

BS.NuGet.Tools = {
  refreshPackagesList : function() {
    $('nugetPackagesList').refresh();
  },

  InstallPopup : OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
      return $('nugetInstallFormDialog');
    },

    formElement : function() {
      return $('nugetInstallForm');
    },

    disableSubmit : function() {
      $('installNuGetApplyButton').disabled = true;
    },

    enableSubmit : function() {
      $('installNuGetApplyButton').disabled = false;
    },

    refreshForm : function() {
      var that = this;
      that.enableSubmit();
      $('nugetInstallFormResresh').refresh("nugetInstallLinkSaving", null, function() {
        that.showCentered();
      });
      return false;
    },

    show : function() {
      var that = this;
      that.showCentered();
      that.refreshForm();
      return false;
    },

    save : function() {

      BS.Util.show($('installNuGetApplyProgress'));
      BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
        onCompleteSave: function(form, responseXML, err) {
          BS.Util.hide($('installNuGetApplyProgress'));
          var wereErrors = BS.XMLResponse.processErrors(responseXML, {}, form.propertiesErrorsHandler);
          BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);

          alert("wereError = " + wereErrors);
          if (!wereErrors) {
            BS.NuGet.Tools.refreshPackagesList();
            //form.close();
          }
        }
      }));
    }
  }))
};



