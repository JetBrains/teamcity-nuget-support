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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

/**
 * @author Evgeniy.Koshkin
 */
public class MetadataConstants {
  public static final String NUGET_GALLERY_NAMESPACE = "NuGetGallery";
  public static final String ENTITY_SET_NAME = "Packages";
  public static final String ENTITY_TYPE_NAME = "V2FeedPackage";
  public static final String CONTAINER_NAME = "V2FeedContext";
  public static final String HTTP_METHOD_GET = "GET";
  public static final String SEARCH_FUNCTION_NAME = "Search";
  public static final String FIND_PACKAGES_BY_ID_FUNCTION_NAME = "FindPackagesById";
  public static final String GET_UPDATES_FUNCTION_NAME = "GetUpdates";
}
