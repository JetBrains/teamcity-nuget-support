package jetbrains.buildServer.nuget.feed.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * NuGet utilities.
 */
public class NuGetUtils {
    @Nullable
    public static String getValue(@NotNull final Map<String, String> attributes, @NotNull final String key) {
        String value = attributes.get(key);
        if (value == null) {
            return null;
        }

        final StringBuilder valueBuilder = new StringBuilder();
        int index = 1;
        while (value != null) {
            valueBuilder.append(value);
            value = attributes.get(key + index++);
        }

        return valueBuilder.toString();
    }
}
