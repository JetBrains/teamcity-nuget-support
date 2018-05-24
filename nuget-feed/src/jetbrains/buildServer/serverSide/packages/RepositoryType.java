package jetbrains.buildServer.serverSide.packages;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public abstract class RepositoryType implements ServerExtension {
    /**
     * Internal type name, used as id
     *
     * @return see above
     */
    @NotNull
    public abstract String getType();

    /**
     * Displayed name (e.g. in selectors)
     *
     * @return see above
     */
    @NotNull
    public abstract String getName();

    /**
     * Factory method to create a new repository for this type.
     *
     * @param projectId is project identifier.
     * @param parameters is a list of configuration parameters.
     * @return new instance of repository.
     */
    @NotNull
    public abstract Repository createRepository(@NotNull final SProject project,
                                                @NotNull final Map<String, String> parameters);

    /**
     * Url for editing repository parameters
     * @return url for editing repository parameters
     */
    @NotNull
    public abstract String getEditParametersUrl();

    /**
     * Returns default parameters for current {@code RepositoryType}
     * @return default parameters
     */
    @NotNull
    public Map<String, String> getDefaultParameters() {
        return Collections.emptyMap();
    }

    /**
     * Returns parameters processor which will be used to validate parameters specified by user.
     * @return see above
     */
    @Nullable
    public PropertiesProcessor getParametersProcessor() { return null; }
}
