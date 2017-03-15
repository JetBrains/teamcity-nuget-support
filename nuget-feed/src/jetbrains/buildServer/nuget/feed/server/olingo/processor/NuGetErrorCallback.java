/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.olingo.processor;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;

/**
 * Callback for handling errors by logging internal server errors additionally.
 * 
 */
public class NuGetErrorCallback implements ODataErrorCallback {

  private static final Logger LOG = Logger.getInstance(NuGetErrorCallback.class.getName());

  @Override
  public ODataResponse handleError(final ODataErrorContext context) throws ODataApplicationException {
    if (context.getHttpStatus() == HttpStatusCodes.INTERNAL_SERVER_ERROR) {
      LOG.warnAndDebugDetails("Internal Server Error", context.getException());
    }

    return EntityProvider.writeErrorDocument(context);
  }
}
