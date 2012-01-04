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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import com.sun.jersey.api.container.filter.LoggingFilter;
import org.odata4j.jersey.server.JerseyServer;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.01.12 0:04
 */
public class ODataProducerUtil {
  public static ODataServer hostODataServer(String baseUri) {
    return startODataServer(baseUri);
  }

  public static ODataServer createODataServer(String baseUri) {
    return new JerseyServer(baseUri, DefaultODataApplication.class, RootApplication.class)
            .addJerseyRequestFilter(LoggingFilter.class) // log all requests
            ;
  }

  public static ODataServer startODataServer(String baseUri) {
    return createODataServer(baseUri).start();
  }
}
