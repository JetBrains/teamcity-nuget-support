package jetbrains.buildServer.serverSide.packages.impl;

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorImpl;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.serverSide.packages.Repository;
import jetbrains.buildServer.serverSide.packages.RepositoryConstants;
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry;
import jetbrains.buildServer.serverSide.packages.RepositoryType;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class RepositoryManagerImpl implements RepositoryManager {

    private final RepositoryRegistry myRepositoryRegistry;
    private final ProjectManager myProjectManager;
    private final MetadataStorage myMetadataStorage;

    public RepositoryManagerImpl(@NotNull final RepositoryRegistry repositoryRegistry,
                                 @NotNull final ProjectManager projectManager,
                                 @NotNull final MetadataStorage metadataStorage) {
        myRepositoryRegistry = repositoryRegistry;
        myProjectManager = projectManager;
        myMetadataStorage = metadataStorage;
    }

    @Override
    public void addRepository(@NotNull SProject project, @NotNull Repository repository) {
        project.addFeature(new ProjectFeatureDescriptorImpl(
                getRepositoryNamePrefix(repository.getType().getType()) + repository.getName(),
                RepositoryConstants.PACKAGES_FEATURE_TYPE,
                repository.getParameters(),
                project.getProjectId()
        ));
    }

    @Nullable
    @Override
    public Repository getRepository(@NotNull SProject project, @NotNull String type, @NotNull String name) {
        final SProjectFeatureDescriptor descriptor = project.findFeatureById(getRepositoryNamePrefix(type) + name);
        if (descriptor == null) return null;
        return getRepository(descriptor);
    }

    @Override
    public void removeRepository(@NotNull SProject project, @NotNull String type, @NotNull String name) {
        final SProjectFeatureDescriptor feature = project.findFeatureById(getRepositoryNamePrefix(type) + name);
        if (feature == null) {
            throw new IllegalArgumentException(String.format("Package repository %s not found", name));
        }
        project.removeFeature(feature.getId());

        NuGetFeedData feedData = new NuGetFeedData(project.getExternalId(), name);
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
        return project.findFeatureById(getRepositoryNamePrefix(type) + name) != null;
    }

    @Override
    public void updateRepository(@NotNull SProject project, @NotNull String oldName, @NotNull Repository repository) {
        final String type = repository.getType().getType();
        final SProjectFeatureDescriptor feature = project.findFeatureById(getRepositoryNamePrefix(type) + oldName);
        if (feature == null) {
            throw new IllegalArgumentException(String.format("Package repository %s not found", oldName));
        }

        if (oldName.equals(repository.getName())) {
            project.updateFeature(feature.getId(), feature.getType(), repository.getParameters());
        } else {
            addRepository(project, repository);
            project.removeFeature(feature.getId());
            NuGetFeedData feedData = new NuGetFeedData(project.getExternalId(), oldName);
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
        return repositoryType.createRepository(project.getExternalId(), parameters);
    }

    @NotNull
    private static String getRepositoryNamePrefix(@NotNull String type) {
        return String.format("%s-%s-", RepositoryConstants.PACKAGES_ID_PREFIX, type);
    }
}
