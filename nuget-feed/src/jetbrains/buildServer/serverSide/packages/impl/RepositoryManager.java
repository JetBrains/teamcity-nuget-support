package jetbrains.buildServer.serverSide.packages.impl;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.packages.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface RepositoryManager {

    void addRepository(@NotNull SProject project, @NotNull Repository repository);

    @Nullable
    Repository getRepository(@NotNull SProject project, @NotNull String type, @NotNull String name);

    void removeRepository(@NotNull SProject project, @NotNull String type, @NotNull String name);

    @NotNull
    Collection<Repository> getRepositories(@NotNull SProject project, boolean includeParent);

    boolean hasRepository(@NotNull SProject project, @NotNull String type, @NotNull String name);

    void updateRepository(@NotNull SProject project, @NotNull String oldName, @NotNull Repository repository);
}
