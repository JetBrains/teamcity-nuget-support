

package jetbrains.buildServer.nuget.feed.server.odata4j;

import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.AbstractODataApplication;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.resources.ExceptionMappingProvider;

import java.util.HashSet;
import java.util.Set;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 05.01.12 0:06
*/
public class NuGetODataApplication extends AbstractODataApplication {
  private final NuGetProducerHolder myProducer;

  public NuGetODataApplication(@NotNull final NuGetProducerHolder producer) {
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

  @Override
  public Set<Class<?>> getClasses() {
    final Set<Class<?>> classes = super.getClasses();
    classes.remove(ExceptionMappingProvider.class);
    classes.add(NuGetExceptionMappingProvider.class);
    classes.add(NuGetFeedResponseListener.class);
    return classes;
  }
}
