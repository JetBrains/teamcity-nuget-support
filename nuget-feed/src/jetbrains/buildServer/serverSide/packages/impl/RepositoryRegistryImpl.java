package jetbrains.buildServer.serverSide.packages.impl;

import jetbrains.buildServer.serverSide.packages.RepositoryRegistry;
import jetbrains.buildServer.serverSide.packages.RepositoryType;
import jetbrains.buildServer.serverSide.packages.RepositoryUsagesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RepositoryRegistryImpl implements RepositoryRegistry {

    private Map<String, RepositoryType> myTypes = new HashMap<>();
    private Map<String, RepositoryUsagesProvider> myUsagesProviders = new HashMap<>();

    @Override
    public void register(@NotNull final RepositoryType repositoryType) {
        myTypes.put(repositoryType.getType(), repositoryType);
    }

    @Override
    public void register(@NotNull RepositoryUsagesProvider usagesProvider) {
        myUsagesProviders.put(usagesProvider.getType(), usagesProvider);
    }

    @Nullable
    @Override
    public RepositoryType findType(@NotNull String type) {
        return myTypes.get(type);
    }

    @Nullable
    @Override
    public RepositoryUsagesProvider findUsagesProvider(@NotNull String type) {
        return myUsagesProviders.get(type);
    }

  @NotNull
    @Override
    public Collection<RepositoryType> getTypes() {
        return Collections.unmodifiableCollection(myTypes.values());
    }
}
