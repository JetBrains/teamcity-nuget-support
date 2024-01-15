

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunctions;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.xppimpl.XmlPullXMLFactoryProvider2;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:09
 */
public class NuGetProducerHolder {
  private final NuGetFeedInMemoryProducer myProducer;


  public NuGetProducerHolder(@NotNull final NuGetFeed feed, @NotNull final NuGetAPIVersion apiVersion) {
    final NuGetFeedFunctions functions = new NuGetFeedFunctions(feed);
    //Workaround for Xml generation. Default STAX xml writer
    //used to generate <foo></foo> that is badly parsed in
    //.NET OData WCF client
    XMLFactoryProvider2.setInstance(new XmlPullXMLFactoryProvider2());
    myProducer = new NuGetFeedInMemoryProducer(feed, functions, apiVersion);
    myProducer.register((context) -> {
      boolean includeSemVer2 = ODataUtilities.includeSemVer2(context.getQueryInfo());
      return CollectionsUtil.convertCollection(feed.getAll(includeSemVer2), PackageEntityEx::new);
    });
  }

  @NotNull
  public ODataProducer getProducer() {
    return myProducer;
  }
}
