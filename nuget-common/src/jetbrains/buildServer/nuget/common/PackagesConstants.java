/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.ArtifactsConstants;
import jetbrains.buildServer.agent.Constants;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:56
 */
public interface PackagesConstants {
  public static final String INSTALL_RUN_TYPE = "jb.nuget.installer"; //no more than 30 chars
  public static final String PUBLISH_RUN_TYPE = "jb.nuget.publish"; //run-type could never exceed 30 chars
  public static final String PACK_RUN_TYPE = "jb.nuget.pack"; //run-type could never exceed 30 chars
  public static final String ATHU_FEATURE_TYPE = "jb.nuget.auth"; //run-type could never exceed 30 chars

  public static final String[] ALL_NUGET_RUN_TYPES = {INSTALL_RUN_TYPE, PUBLISH_RUN_TYPE, PACK_RUN_TYPE};

  public static final String NUGET_PATH = "nuget.path";
  public static final String NUGET_SOURCES = "nuget.sources";
  public static final String NUGET_EXCLUDE_VERSION = "nuget.excludeVersion";
  public static final String NUGET_NO_CACHE = "nuget.noCache";

  public static final String NUGET_UPDATE_PACKAGES = "nuget.updatePackages";
  public static final String NUGET_UPDATE_PACKAGES_SAFE = "nuget.updatePackages.safe";
  public static final String NUGET_UPDATE_PACKAGES_PRERELEASE = "nuget.updatePackages.include.prerelease";
  public static final String NUGET_UPDATE_PACKAGE_IDS = "nuget.updatePackages.ids";
  public static final String NUGET_UPDATE_MODE = "nuget.updatePackages.mode";

  public static final String SLN_PATH = "sln.path";

  public static final String NUGET_USED_PACKAGES_DIR = ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + "/nuget";
  public static final String NUGET_USED_PACKAGES_FILE = "nuget.xml";


  public static final String NUGET_PUBLISH_CREATE_ONLY = "nuget.publish.create.only";
  public static final String NUGET_PUBLISH_FILES = "nuget.publish.files";
  public static final String NUGET_PUBLISH_SOURCE = "nuget.publish.source";
  public static final String NUGET_API_KEY = Constants.SECURE_PROPERTY_PREFIX + "nuget.api.key";


  public static final String NUGET_TOOL_NAME_PREFIX = "nuget-commnadline-";
  public static final String NUGET_TOOL_REL_PATH = "tools/NuGet.exe";


  public static final String NUGET_PACK_OUTPUT_CLEAR = "nuget.pack.output.clean";
  public static final String NUGET_PACK_OUTPUT_DIR = "nuget.pack.output.directory";
  public static final String NUGET_PACK_PUBLISH_ARTIFACT = "nuget.pack.as.artifact";
  public static final String NUGET_PACK_PREFER_PROJECT = "nuget.pack.prefer.project";
  public static final String NUGET_PACK_BASE_DIRECTORY_MODE = "nuget.pack.project.dir";
  public static final String NUGET_PACK_BASE_DIR = "nuget.pack.base.directory";
  public static final String NUGET_PACK_VERSION = "nuget.pack.version";
  public static final String NUGET_PACK_SPEC_FILE = "nuget.pack.specFile";
  public static final String NUGET_PACK_EXCLUDE_FILES = "nuget.pack.excludes";
  public static final String NUGET_PACK_PROPERTIES = "nuget.pack.properties";
  public static final String NUGET_PACK_CUSOM_COMMANDLINE= "nuget.pack.commandline";
  public static final String NUGET_PACK_AS_TOOL= "nuget.pack.pack.mode.tool";
  public static final String NUGET_PACK_INCLUDE_SOURCES = "nuget.pack.include.sources" ;

  public static final String NUGET_AUTH_FEED = "nuget.auth.feed";
  public static final String NUGET_AUTH_USERNAME = "nuget.auth.username";
  public static final String NUGET_AUTH_PASSWORD = Constants.SECURE_PROPERTY_PREFIX + "nuget.auth.password";
}
