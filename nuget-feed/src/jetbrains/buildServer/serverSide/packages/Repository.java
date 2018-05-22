package jetbrains.buildServer.serverSide.packages;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public abstract class Repository {
    private final RepositoryType myType;
    private final String myProjectId;
    private final Map<String, String> myParameters;

    public Repository(@NotNull final RepositoryType type,
                      @NotNull final String projectId,
                      @NotNull final Map<String, String> parameters) {
        myType = type;
        myProjectId = projectId;
        myParameters = parameters;
    }

    @NotNull
    public String getProjectId() {
        return myProjectId;
    }

    @NotNull
    public String getName() {
        return myParameters.get(RepositoryConstants.REPOSITORY_NAME_KEY);
    }

    @NotNull
    public RepositoryType getType() {
        return myType;
    }

    @NotNull
    public String getDescription() {
        return myParameters.get(RepositoryConstants.REPOSITORY_DESCRIPTION_KEY);
    }

    @NotNull
    public Map<String, String> getParameters() {
        return myParameters;
    }

    public abstract List<String> getUrlPaths();
}
