package jetbrains.buildServer.serverSide.packages;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

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
        return sanitize(myParameters.get(RepositoryConstants.REPOSITORY_NAME_KEY));
    }

    @NotNull
    public RepositoryType getType() {
        return myType;
    }

    @NotNull
    public String getDescription() {
        return sanitize(myParameters.get(RepositoryConstants.REPOSITORY_DESCRIPTION_KEY));
    }

    @NotNull
    public Map<String, String> getParameters() {
        return myParameters;
    }

    @NotNull
    public String getParametersDescription() {
      return "";
    }

    @NotNull
    public List<String> getUrlPaths() {
      return Collections.emptyList();
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    @Nullable
    private static String sanitize(@Nullable final String string) {
      if(string == null) {
        return null;
      }

      return string
        .replaceAll("(?i)<script.*?>.*?</script.*?>", "")
        .replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "")
        .replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "")
        .replace('<', ' ')
        .replace('>', ' ')
        .replace('&', ' ')
        .replace('\'', ' ')
        .replace('"', ' ');
    }
}
