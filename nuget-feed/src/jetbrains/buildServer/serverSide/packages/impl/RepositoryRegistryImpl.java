package jetbrains.buildServer.serverSide.packages.impl;

import jetbrains.buildServer.serverSide.packages.RepositoryRegistry;
import jetbrains.buildServer.serverSide.packages.RepositoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RepositoryRegistryImpl implements RepositoryRegistry {

    private Map<String, RepositoryType> myTypes = new HashMap<String, RepositoryType>();

    @Override
    public void register(@NotNull final RepositoryType repositoryType) {
        myTypes.put(repositoryType.getType(), repositoryType);
    }

    @Nullable
    @Override
    public RepositoryType findType(@NotNull String type) {
        return myTypes.get(type);
    }

    @NotNull
    @Override
    public Collection<RepositoryType> getTypes() {
        return Collections.unmodifiableCollection(myTypes.values());
    }
}
