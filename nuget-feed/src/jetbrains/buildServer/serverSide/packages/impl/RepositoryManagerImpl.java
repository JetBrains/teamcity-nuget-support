package jetbrains.buildServer.serverSide.packages.impl;

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorFactory;
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorImpl;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.serverSide.packages.Repository;
import jetbrains.buildServer.serverSide.packages.RepositoryConstants;
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry;
import jetbrains.buildServer.serverSide.packages.RepositoryType;
import jetbrains.buildServer.util.CachingTypedIdGenerator;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class RepositoryManagerImpl implements RepositoryManager, CachingTypedIdGenerator {

    private final RepositoryRegistry myRepositoryRegistry;
    private final ProjectManager myProjectManager;
    private final MetadataStorage myMetadataStorage;

    public RepositoryManagerImpl(@NotNull final RepositoryRegistry repositoryRegistry,
                                 @NotNull final ProjectManager projectManager,
                                 @NotNull final MetadataStorage metadataStorage,
                                 @NotNull final ProjectFeatureDescriptorFactory descriptorFactory) {
        myRepositoryRegistry = repositoryRegistry;
        myProjectManager = projectManager;
        myMetadataStorage = metadataStorage;
        descriptorFactory.registerGenerator(RepositoryConstants.PACKAGES_FEATURE_TYPE, this);
    }

    @Override
    public void addRepository(@NotNull SProject project, @NotNull Repository repository) {
        project.addFeature(new ProjectFeatureDescriptorImpl(
                getProjectFeatureName(repository.getType().getType(), repository.getName()),
                RepositoryConstants.PACKAGES_FEATURE_TYPE,
                repository.getParameters(),
                project.getProjectId()
        ));
        project.persist();
    }

    @Nullable
    @Override
    public Repository getRepository(@NotNull SProject project, @NotNull String type, @NotNull String name) {
        final SProjectFeatureDescriptor descriptor = project.findFeatureById(getProjectFeatureName(type, name));
        if (descriptor == null) return null;
        return getRepository(descriptor);
    }

    @Override
    public void removeRepository(@NotNull SProject project, @NotNull String type, @NotNull String name) {
        final SProjectFeatureDescriptor feature = project.findFeatureById(getProjectFeatureName(type, name));
        if (feature == null) {
            throw new IllegalArgumentException(String.format("Package repository %s not found", name));
        }
        project.removeFeature(feature.getId());
        project.persist();

        NuGetFeedData feedData = new NuGetFeedData(project.getProjectId(), name);
        Iterator<BuildMetadataEntry> entries = myMetadataStorage.getAllEntries(feedData.getKey());
        while (entries.hasNext()) {
            BuildMetadataEntry entry = entries.next();
            myMetadataStorage.removeBuildEntries(entry.getBuildId(), feedData.getKey());
        }
    }

    @NotNull
    @Override
    public Collection<Repository> getRepositories(@NotNull SProject project, boolean includeParent) {
        final Collection<SProjectFeatureDescriptor> features;
        if (includeParent) {
            features = project.getAvailableFeaturesOfType(RepositoryConstants.PACKAGES_FEATURE_TYPE);
        } else {
            features = project.getOwnFeaturesOfType(RepositoryConstants.PACKAGES_FEATURE_TYPE);
        }

        return CollectionsUtil.convertAndFilterNulls(features, this::getRepository);
    }

    @Override
    public boolean hasRepository(@NotNull SProject project, @NotNull String type, @NotNull String name) {
        return project.findFeatureById(getProjectFeatureName(type, name)) != null;
    }

    @Override
    public void updateRepository(@NotNull SProject project, @NotNull String oldName, @NotNull Repository repository) {
        final String type = repository.getType().getType();
        final SProjectFeatureDescriptor feature = project.findFeatureById(getProjectFeatureName(type, oldName));
        if (feature == null) {
            throw new IllegalArgumentException(String.format("Package repository %s not found", oldName));
        }

        if (oldName.equals(repository.getName())) {
            project.updateFeature(feature.getId(), feature.getType(), repository.getParameters());
            project.persist();
        } else {
            addRepository(project, repository);
            project.removeFeature(feature.getId());
            project.persist();

            NuGetFeedData feedData = new NuGetFeedData(project.getProjectId(), oldName);
            Iterator<BuildMetadataEntry> entries = myMetadataStorage.getAllEntries(feedData.getKey());
            while (entries.hasNext()) {
                BuildMetadataEntry entry = entries.next();
                myMetadataStorage.addBuildEntry(entry.getBuildId(), feedData.getKey(), entry.getKey(), entry.getMetadata(), true);
                myMetadataStorage.removeBuildEntries(entry.getBuildId(), feedData.getKey());
            }
        }
    }

    @Nullable
    private Repository getRepository(SProjectFeatureDescriptor feature) {
        final Map<String, String> parameters = feature.getParameters();
        final String type = parameters.get(RepositoryConstants.REPOSITORY_TYPE_KEY);
        if (type == null) return null;

        final RepositoryType repositoryType = myRepositoryRegistry.findType(type);
        if (repositoryType == null) return null;

        SProject project = myProjectManager.findProjectById(feature.getProjectId());
        if (project == null) {
            return null;
        }
        return repositoryType.createRepository(project, parameters);
    }

    @NotNull
    private static String getProjectFeatureName(@NotNull String type, @NotNull String name) {
        return String.format("%s-%s-%s", RepositoryConstants.PACKAGES_ID_PREFIX, type, name);
    }

    @Override
    public void addGeneratedId(@NotNull String id) {
    }

    @Nullable
    @Override
    public String newId(@NotNull Map<String, String> parameters) {
        final String type = parameters.get(RepositoryConstants.REPOSITORY_TYPE_KEY);
        if (type == null) return null;

        final String name = parameters.get(RepositoryConstants.REPOSITORY_NAME_KEY);
        if (name == null) return null;

        return getProjectFeatureName(type, name);
    }
}
