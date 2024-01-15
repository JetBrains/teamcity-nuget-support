

package jetbrains.buildServer.nuget.feed.server.odata4j.entity;

import org.odata4j.core.OAtomStreamEntity;

/**
 * Marker interface to implement V1 and V2 ccmpatible feed
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 */
public interface PackageEntity extends PackageEntityV2, OAtomStreamEntity {

  String[] KeyPropertyNames = PackageEntityV2.KeyPropertyNames;
}
