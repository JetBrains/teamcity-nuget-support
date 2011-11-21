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
   Form : OO.extend(BS.AbstractWebForm, {
     formElement : function() {
       return $('nugetSettingsForm');
     },

     saveForm : function() {
       var that = this;
       BS.Util.show($('nugetSettingsSaving'));
       BS.Util.hide($('nugetSettingsSuccessMessage'));
       BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
         onCompleteSave: function() {
           BS.Util.hide($('nugetSettingsSaving'));
           BS.Util.reenableForm(that.formElement());
           BS.NuGet.FeedServer.refreshStatus();
           BS.Util.show($('nugetSettingsSuccessMessage'));
         }
       }));
       return false;
     }
   }),

   refreshStatus : function() {
     $('nugetServerStatus').refresh();
   },

   registerStatusRefresh : function() {
     var that = this;
     setTimeout(function() {
       that.refreshStatus();
       that.registerStatusRefresh();
     }, 1000);
   },

   persistCheckbox : function() {
     setTimeout(function() {
       BS.NuGet.FeedServer.Form.saveFormOnCheckbox();
     }, 100);
   },

   refreshLog : function() {
     $('nugetServerLogs').refresh();
   }
 };

 Event.observe(window, "load", function() {
   BS.NuGet.FeedServer.registerStatusRefresh();
   BS.Util.hide($('nugetSettingsSuccessMessage'));
 });

