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
if (!BS.Packages) BS.Packages = {};

BS.Packages = {
  setPageUrl: function(pageUrl) {
    this.pageUrl = pageUrl;
  },

  getPageUrl: function() {
    return this.pageUrl;
  },

  deleteRepository: function (projectId, projectName, type, typeName, name) {
    var text = "<p>Delete the " + typeName + " '" + name + "' with all contents from the '" +
      $j("<span />").text(projectName).html() + "' project?</p>" +
      "<p>This will also remove all package indexer build features pointing at this " + typeName + ".</p>";
    var url = this.getPageUrl();
    BS.confirmDialog.show({
      text: text,
      actionButtonText: "Delete",
      cancelButtonText: 'Cancel',
      title: "Delete " + typeName,
      action: function () {
        var completed = $j.Deferred();
        BS.ajaxRequest(url, {
          parameters: {projectId: projectId, type: type, name: name, action: "deleteRepository"},
          onComplete: function () {
            completed.resolve();
            BS.reload(true);
          }
        });
        return completed;
      }
    });
  },

  addRepository: function (projectId, type, name) {
    var url = this.getPageUrl();
    BS.ajaxRequest(url, {
      parameters: {projectId: projectId, type: type, name: name, action: "addRepository"},
      onComplete: function () {
        BS.reload(true);
      }
    });
  },

  showUrls: function(authType) {
    $j(".authEndpoints a").removeClass('selected');
    $j('.authEndpoints a.' + authType).addClass('selected');
    $j('table.packageSources .details ul').hide();
    $j('table.packageSources .details ul.' + authType).show();
    return false;
  }
};

BS.Packages.AddRepositoryForm = OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractModalDialog, {
  setSaving: function (saving) {
    if (saving) {
      BS.Util.show('newRepositoryProgress');
    } else {
      BS.Util.hide('newRepositoryProgress');
    }
  },

  getContainer: function () {
    return $('newRepositoryFormDialog');
  },

  formElement: function () {
    return $('newRepositoryForm');
  },

  afterClose: function () {
    this.clearContent();
  },

  /**
   * Show dialog
   *
   * @param {string} projectId
   * @param {string} type
   * @param {string} typeName
   * @param {string} name
   */
  showDialog: function (projectId, type, typeName, name) {
    this.projectId = projectId;
    this.type = type;
    this.name = name;

    var action = name ? "Edit " : "Add ";
    $j('#newRepositoryFormTitle').text(action + typeName);
    BS.Util.reenableForm(this.formElement());
    this.showCentered();
    this.bindCtrlEnterHandler(this.submit.bind(this));
    this.refreshDialog();
  },

  submit: function () {
    var url = BS.Packages.getPageUrl() +
      "?action=saveRepository" +
      "&projectId=" + this.projectId +
      (this.name ? "&name=" + this.name : "");
    this.saveForm(url);
  },

  saveForm: function (url) {
    var that = this;
    BS.PasswordFormSaver.save(this, url, OO.extend(BS.ErrorsAwareListener, {
      onCompleteSave: function (form, responseXML, err) {
        that.setSaving(false);
        var wereErrors = BS.XMLResponse.processErrors(responseXML, {
          onInvalidProperties: function () {
            alert('Invalid repository parameters');
          },
          onCannotEditProject: function () {
            alert('TeamCity server is in read only mode');
          },
          onProjectNotFound: function () {
            alert('Selected project not found');
          },
          onSaveFailure: function () {
            alert('Unable to save parameters');
          }
        }, that.propertiesErrorsHandler);

        if (wereErrors) {
          BS.Util.reenableForm(that.formElement());
          return;
        }

        that.close();
        BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);

        if (!err) {
          BS.XMLResponse.processRedirect(responseXML);
        }
        BS.reload(true);
      }
    }));
  },

  clearContent: function () {
    $("newRepositoryDiv").innerHTML = "";
  },

  cancelDialog: function () {
    this.clearContent();
    this.close();
  },

  refreshDialog: function () {
    var that = this;
    that.setSaving(true);
    var url = BS.Packages.getPageUrl() +
      "?projectId=" + that.projectId +
      "&type=" + that.type +
      (that.name ? "&name=" + that.name : "");

    BS.ajaxUpdater($("newRepositoryDiv"), url, {
      method: "get",
      evalScripts: true,
      onComplete: function() {
        that.setSaving(false);
        BS.VisibilityHandlers.updateVisibility("newRepositoryDiv");
        BS.Packages.AddRepositoryForm.recenterDialog();
        $j('#repositoryType').val(that.type);
      }
    });
  }
}));
