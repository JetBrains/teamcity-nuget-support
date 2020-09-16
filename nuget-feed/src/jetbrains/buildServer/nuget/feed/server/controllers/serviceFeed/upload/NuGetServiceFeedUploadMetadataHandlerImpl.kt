package jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadMetadataHandler
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProvider
import jetbrains.buildServer.serverSide.RunningBuildEx
import jetbrains.buildServer.serverSide.impl.LogUtil
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import org.springframework.web.multipart.MultipartHttpServletRequest
import javax.servlet.http.HttpServletResponse

class NuGetServiceFeedUploadMetadataHandlerImpl(
        private val myStorage: MetadataStorage,
        private val myFeedsProvider: NuGetBuildFeedsProvider
) : NuGetFeedUploadMetadataHandler<NuGetServiceFeedUploadHandlerContext> {
    override fun validate(
            request: MultipartHttpServletRequest,
            response: HttpServletResponse,
            context: NuGetServiceFeedUploadHandlerContext,
            build: RunningBuildEx,
            key: String,
            metadata: MutableMap<String, String>
    ) {
    }

    override fun handleMetadata(
            request: MultipartHttpServletRequest,
            response: HttpServletResponse,
            context: NuGetServiceFeedUploadHandlerContext,
            build: RunningBuildEx,
            key: String,
            metadata: MutableMap<String, String>
    ) {
        val targetFeeds = myFeedsProvider.getFeeds(build)
        if (targetFeeds.isEmpty()) {
            LOG.debug("No NuGet feeds found to index packages from build ${LogUtil.describe(build)}")
            return
        }

        for (targetFeed in targetFeeds) {
            try {
                if (LOG.isDebugEnabled) {
                    LOG.debug("Adding metadata entry for package $key in build ${jetbrains.buildServer.log.LogUtil.describe(build)}")
                }

                myStorage.addBuildEntry(build.buildId, targetFeed.key, key, metadata, !build.isPersonal)

                if (LOG.isDebugEnabled) {
                    LOG.debug("Added metadata entry for package $key in build ${jetbrains.buildServer.log.LogUtil.describe(build)}")
                }
            } catch (e: Throwable) {
                LOG.warnAndDebugDetails("Failed to update metadata for build $build in project ${targetFeed.projectExtId} feed ${targetFeed.feedId}. Error: ${e.message}", e)
                throw e
            }
        }
    }

    companion object {
        val LOG = Logger.getInstance(NuGetServiceFeedUploadMetadataHandlerImpl::class.java.name)
    }
}
