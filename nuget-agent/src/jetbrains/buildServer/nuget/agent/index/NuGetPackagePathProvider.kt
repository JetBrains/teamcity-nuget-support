package jetbrains.buildServer.nuget.agent.index

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.impl.artifacts.ArchivePreprocessor

class NuGetPackagePathProvider(private val extensions: ExtensionHolder) {
    fun getArtifactPath(path: String, fileName: String): String {
        val targetPath = path.trimEnd('\\', '/')
        if (targetPath.isEmpty()) {
            return fileName
        }

        if (!targetPath.contains(ARCHIVE_PATH_SEPARATOR)) {
            for (preprocessor in extensions.getExtensions(ArchivePreprocessor::class.java)) {
                preprocessor.getTargetKey(targetPath)?.let {archivePath ->
                    if (archivePath.isNotEmpty()) {
                        return archivePath + ARCHIVE_PATH_SEPARATOR + fileName
                    }
                }
            }
        }

        return "$targetPath/$fileName"
    }

    companion object {
        private const val ARCHIVE_PATH_SEPARATOR = "!/"
    }
}
