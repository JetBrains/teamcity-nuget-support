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

import com.intellij.openapi.diagnostic.Logger;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.monitoring.ResponseListener;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Evgeniy.Koshkin
 */
@Provider
public class NuGetFeedResponseListener implements ResponseListener {

  private final Logger LOG = Logger.getInstance(getClass().getName());

  public void onError(long id, Throwable ex) {
    LOG.warnAndDebugDetails("Error on processing NuGet feed response with ID " + id, ex);
  }

  public void onResponse(long id, ContainerResponse response) {
  }

  public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
    LOG.warnAndDebugDetails("Exception on processing NuGet feed response with ID " + id, exception);
  }
}
