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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.AbstractODataApplication;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

import java.util.HashSet;
import java.util.Set;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 05.01.12 0:06
*/
public class NuGetODataApplication extends AbstractODataApplication {
  private final NuGetProducer myProducer;

  NuGetODataApplication(@NotNull final NuGetProducer producer) {
    myProducer = producer;
  }

  @Override
  public Set<Object> getSingletons() {
    final Set<Object> set = new HashSet<Object>(super.getSingletons());
    set.add(new DefaultODataProducerProvider(){
      @Override
      protected ODataProducer createInstanceFromFactoryInContainerSpecificSetting() {
        return myProducer.getProducer();
      }
    });
    return set;
  }
}
