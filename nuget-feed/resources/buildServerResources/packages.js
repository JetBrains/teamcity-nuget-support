

if (!BS) BS = {};
if (!BS.Packages) BS.Packages = {};

BS.Packages = {
  setPageUrl: function(pageUrl) {
    this.pageUrl = pageUrl;
  },

  getPageUrl: function() {
    return this.pageUrl;
  },

  _getUsagesNote: function(typeName, usagesCount, action) {
      if (!usagesCount) return "";
      return "<div class='attentionComment'>This " + typeName + " is used by " +
      (usagesCount >= 100 ? "100+" : usagesCount) + " build(s) and " + action + " it may cause dependent build failures.</div>";
  },

  deleteRepository: function (projectId, projectName, type, typeName, name, usagesCount) {
    var text = "<p>Delete the " + typeName + " '" + name + "' with all contents from the '" +
      $j("<span />").text(projectName).html() + "' project?</p>" +
      BS.Packages._getUsagesNote(typeName, usagesCount, "deleting");
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
   * @param {string} usagesCount
   */
  showDialog: function (projectId, type, typeName, name, usagesCount) {
    this.projectId = projectId;
    this.type = type;
    this.typeName = typeName;
    this.name = name;
    this.usagesCount = usagesCount;

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
        $j('input[name="prop:name"]').on('input paste', function () {
           if (that.usagesCount > 0 && that.name && that.name != this.value) {
               $j('#usagesNoteDiv').html(BS.Packages._getUsagesNote(that.typeName, that.usagesCount, "renaming"));
           } else {
               $j('#usagesNoteDiv').html("");
           }
        });
      }
    });
  }
}));
