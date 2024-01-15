

package jetbrains.buildServer.nuget.agent.runner.install.impl;

import java.io.File;

public class PathUtils {
    public static String normalize(final String path)
    {
        if (path == null) {
            return null;
        }

        return path.replace("\\", File.separator);
    }
}
