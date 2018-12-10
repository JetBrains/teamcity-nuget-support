package jetbrains.buildServer.serverSide.packages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface RepositoryRegistry {

    void register(@NotNull RepositoryType repositoryType);

    void register(@NotNull RepositoryUsagesProvider usagesProvider);

    @Nullable
    RepositoryType findType(@NotNull String type);

    @Nullable
    RepositoryUsagesProvider findUsagesProvider(@NotNull String type);

    @NotNull
    Collection<RepositoryType> getTypes();
}
