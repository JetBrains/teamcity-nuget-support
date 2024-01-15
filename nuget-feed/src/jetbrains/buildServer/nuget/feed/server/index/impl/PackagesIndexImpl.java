

package jetbrains.buildServer.nuget.feed.server.index.impl;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.index.NuGetServerStatisticsProvider;
import jetbrains.buildServer.nuget.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.feed.server.index.impl.latest.LatestCalculator;
import jetbrains.buildServer.nuget.feed.server.index.impl.latest.LatestVersionsCalculator;
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.IsLatestFieldTransformation;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:18
 */
public class PackagesIndexImpl implements PackagesIndex, NuGetServerStatisticsProvider {

  private static final Logger LOG = Logger.getInstance(PackagesIndexImpl.class.getName());

  public static final Collection<String> PACKAGE_ATTRIBUTES_TO_SEARCH = Lists.newArrayList(
    ID, TITLE, TAGS, DESCRIPTION, AUTHORS, SUMMARY);

  private static final String TOTAL_NUMBER_OF_ITEMS_STAT = "Total number of items in index";
  private static final String NUMBER_OF_INDEXED_BUILDS_STAT = "Number of indexed builds";
  private static final String NUMBER_OF_PACKAGE_IDS_STAT = "Number of unique package Ids";

  private final NuGetFeedData myFeedData;
  private final MetadataStorage myStorage;
  private final Collection<PackageTransformation> myTransformations;


  public PackagesIndexImpl(@NotNull final NuGetFeedData feedData,
                           @NotNull final MetadataStorage storage,
                           @NotNull final Collection<PackageTransformation> transformations) {
    myFeedData = feedData;
    myStorage = storage;
    myTransformations = new ArrayList<>(transformations);
  }

  @NotNull
  @Override
  public List<NuGetIndexEntry> getAll() {
    return decorateMetadata(myStorage.getAllEntries(myFeedData.getKey()));
  }

  @NotNull
  @Override
  public List<NuGetIndexEntry> getForBuild(long buildId) {
    return decorateMetadata(myStorage.getBuildEntry(buildId, myFeedData.getKey()));
  }

  @NotNull
  @Override
  public List<NuGetIndexEntry> find(@NotNull Map<String, String> query) {
    return decorateMetadata(myStorage.findEntriesWithKeyValuePairs(myFeedData.getKey(), query, true));
  }

  @NotNull
  public List<NuGetIndexEntry> search(@NotNull Collection<String> keys, @NotNull String value) {
    return decorateMetadata(myStorage.findEntriesWithValue(myFeedData.getKey(), value, keys, true));
  }

  @NotNull
  @Override
  public List<NuGetIndexEntry> getByKey(String key) {
    return decorateMetadata(myStorage.getEntriesByKey(myFeedData.getKey(), key));
  }

  private List<NuGetIndexEntry> decorateMetadata(Iterator<BuildMetadataEntry> entries) {
    if (TeamCityProperties.getBoolean("teamcity.nuget.simple.feed.sort")) {
      final Collection<PackageTransformation> transformations = getTranslatorsSimple();
      return convertCollection(entries, source -> {
        final NuGetPackageBuilder pb = applyTransformation(source, transformations);
        if (pb == null) return null;
        return pb.build();
      });
    }

    final Collection<PackageTransformation> translators = getTranslators(); //contains processing state!
    final LatestCalculator latestPackages = new LatestVersionsCalculator();
    final List<NuGetPackageBuilder> result = convertCollection(entries, source -> {
      final NuGetPackageBuilder builder = applyTransformation(source, translators);
      if (builder != null) {
        latestPackages.updatePackage(builder);
      }
      return builder;
    });

    latestPackages.updateSelectedPackages();

    //This is most consuming operation that requires to sort collection of entire packages
    result.sort(SemanticVersionsComparators.getBuildersComparator());

    return CollectionsUtil.convertCollection(result, NuGetPackageBuilder::build);
  }

  @NotNull
  private Collection<PackageTransformation> getTranslators() {
    List<PackageTransformation> list = new ArrayList<>(myTransformations.size() + 1);
    for (PackageTransformation t : myTransformations) {
      list.add(t.createCopy());
    }
    return list;
  }

  @NotNull
  private Collection<PackageTransformation> getTranslatorsSimple() {
    final Collection<PackageTransformation> pts = getTranslators();
    pts.add(new IsLatestFieldTransformation());
    return pts;
  }

  @Nullable
  private NuGetPackageBuilder applyTransformation(@NotNull final BuildMetadataEntry entry,
                                                         @NotNull final Collection<PackageTransformation> transformations) {
    try {
      final NuGetPackageBuilder pb = new NuGetPackageBuilder(myFeedData, entry);
      for (PackageTransformation transformation : transformations) {
        if (transformation.applyTransformation(pb) == PackageTransformation.Status.SKIP) return null;
      }
      return pb;
    } catch (Exception ex) {
      LOG.warnAndDebugDetails("Failed to convert build metadata entry to nuget package. Entry: " + entry.toString(), ex);
      return null;
    }
  }

  @NotNull
  public Map<String, Long> getIndexStatistics() {
    final Iterator<BuildMetadataEntry> entries = myStorage.getAllEntries(myFeedData.getKey());

    long totalItemsNumber = 0;
    Set<Long> buildIds = new HashSet<>();
    Set<String> packageIds = new HashSet<>();

    while (entries.hasNext()) {
      final BuildMetadataEntry entry = entries.next();
      totalItemsNumber++;
      buildIds.add(entry.getBuildId());
      packageIds.add(entry.getMetadata().get(ID));
    }

    final Map<String, Long> stats = new HashMap<>();
    stats.put(TOTAL_NUMBER_OF_ITEMS_STAT, totalItemsNumber);
    stats.put(NUMBER_OF_INDEXED_BUILDS_STAT, (long) buildIds.size());
    stats.put(NUMBER_OF_PACKAGE_IDS_STAT, (long) packageIds.size());
    return stats;
  }

  private static <ResultType, SourceType> List<ResultType> convertCollection(Iterator<? extends SourceType> source,
                                                                             Converter<ResultType, SourceType> converter) {
    final List<ResultType> result = new ArrayList<>();
    while (source.hasNext()) {
      ResultType converted = converter.createFrom(source.next());
      if (converted != null) {
        result.add(converted);
      }
    }
    return result;
  }
}
