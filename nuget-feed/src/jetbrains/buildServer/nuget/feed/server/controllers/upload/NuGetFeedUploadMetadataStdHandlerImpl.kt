package jetbrains.buildServer.nuget.feed.server.controllers.upload

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.log.LogUtil
import jetbrains.buildServer.nuget.common.PackageExistsException
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.serverSide.RunningBuildEx
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import org.springframework.web.multipart.MultipartHttpServletRequest
import javax.servlet.http.HttpServletResponse

class NuGetFeedUploadMetadataStdHandlerImpl(private val myStorage: MetadataStorage) : NuGetFeedUploadMetadataHandler<NuGetFeedUploadHandlerStdContext> {
    override fun validate(request: MultipartHttpServletRequest, response: HttpServletResponse, context: NuGetFeedUploadHandlerStdContext, build: RunningBuildEx, key: String, metadata: MutableMap<String, String>) {
        val replace = "true".equals(request.getParameter("replace"), true);

        // Packages must not exists in the feed if `replace=true` query parameter was not specified
        if (!replace && myStorage.getEntriesByKey(context.feedData.key, key).hasNext()) {
            throw PackageExistsException("NuGet package ${metadata.get(NuGetPackageAttributes.ID)}:${metadata.get(NuGetPackageAttributes.NORMALIZED_VERSION)} already exists " +
                    "in the project ${context.feedData.projectExtId} feed ${context.feedData.feedId}")
        }
    }

    override fun handleMetadata(request: MultipartHttpServletRequest, response: HttpServletResponse, context: NuGetFeedUploadHandlerStdContext, build: RunningBuildEx, key: String, metadata: MutableMap<String, String>) {
        try {
            LOG.debug("Adding metadata entry for package $key in build ${LogUtil.describe(build)}")
            myStorage.addBuildEntry(build.buildId, context.feedData.key, key, metadata, !build.isPersonal)
        } catch (e: Throwable) {
            LOG.warnAndDebugDetails("Failed to update metadata for build $build in project ${context.feedData.projectExtId} feed ${context.feedData.feedId}. Error: ${e.message}", e)
            throw e
        } finally {
            LOG.debug("Added metadata entry for package $key in build ${LogUtil.describe(build)}")
        }
    }

    companion object {
        val LOG = Logger.getInstance(NuGetFeedUploadMetadataStdHandlerImpl::class.java.name)
    }
}
