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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 12:10
 */
public class MetadataControllerRegistrar {
  public MetadataControllerRegistrar(@NotNull final WebControllerManager web,
                                     @NotNull final Collection<MetadataControllerBase> controllers,
                                     @NotNull final AuthorizationInterceptor authz) {
    for (MetadataControllerBase controller : controllers) {
      String path = controller.getControllerPath();
      authz.addPathNotRequiringAuth(path);
      web.registerController(path, controller);
    }
  }
}
