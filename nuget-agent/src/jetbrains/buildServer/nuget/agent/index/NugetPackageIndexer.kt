package jetbrains.buildServer.nuget.agent.index

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsBuilderAdapter
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection
import jetbrains.buildServer.nuget.common.FeedConstants
import jetbrains.buildServer.nuget.common.NuGetServerConstants
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.nuget.common.index.ODataDataFormat
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_ARTIFACT_RELPATH
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_BUILD_TYPE_ID
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*
import jetbrains.buildServer.util.EventDispatcher
import java.io.File
import java.util.*

class NugetPackageIndexer(dispatcher: EventDispatcher<AgentLifeCycleListener>,
                          private val packageAnalyzer: PackageAnalyzer,
                          private val packagePublisher: NuGetPackagePublisher,
                          private val pathProvider: NuGetPackagePathProvider)
    : ArtifactsBuilderAdapter() {

    private var myIndexingEnabled = false
    private val myPackages = arrayListOf<NuGetPackageData>()
    private lateinit var myBuildType: String
    private lateinit var myLogger: BuildProgressLogger

    init {
        dispatcher.addListener(object : AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                myPackages.clear()
                myIndexingEnabled = runningBuild.sharedConfigParameters[NuGetServerConstants.FEED_AGENT_SIDE_INDEXING]?.toBoolean() ?: false
                myBuildType = runningBuild.buildTypeId
                myLogger = runningBuild.buildLogger
            }

            override fun afterAtrifactsPublished(runningBuild: AgentRunningBuild, status: BuildFinishedStatus) {
                try {
                    packagePublisher.publishPackages(myPackages)
                } catch (e: Exception) {
                    val message = "Failed to write NuGet packages metadata"
                    myLogger.warning("$message: ${e.message}")
                    LOG.warnAndDebugDetails(message, e)
                }
            }
        })
    }

    override fun afterCollectingFiles(artifacts: MutableList<ArtifactsCollection>) {
        if (!myIndexingEnabled) {
            return
        }

        getPackages(artifacts).forEach { (file, path) ->
            pathProvider.getArtifactPath(path, file.name)?.let { packagePath ->
                try {
                    val metadata = readMetadata(file, packagePath)
                    myPackages.add(NuGetPackageData(packagePath, metadata))
                } catch (e: Exception) {
                    val message = "Failed to read NuGet package $packagePath contents"
                    myLogger.warning("$message: ${e.message}")
                    LOG.warnAndDebugDetails(message, e)
                }
            }
        }
    }

    private fun readMetadata(file: File, packagePath: String): MutableMap<String, String> {
        val metadata = file.inputStream().use {
            packageAnalyzer.analyzePackage(it)
        }

        file.inputStream().use {
            metadata[PACKAGE_HASH] = packageAnalyzer.getSha512Hash(it)
            metadata[PACKAGE_HASH_ALGORITHM] = PackageAnalyzer.SHA512
        }

        val created = ODataDataFormat.formatDate(Date())
        metadata[CREATED] = created
        metadata[LAST_UPDATED] = created
        metadata[PUBLISHED] = created
        metadata[PACKAGE_SIZE] = file.length().toString()
        metadata[TEAMCITY_ARTIFACT_RELPATH] = packagePath
        metadata[TEAMCITY_BUILD_TYPE_ID] = myBuildType

        return metadata
    }

    private fun getPackages(artifactsCollections: List<ArtifactsCollection>): Map<File, String> {
        val result = HashMap<File, String>()
        artifactsCollections.forEach {
            it.filePathMap.forEach { (artifact, path) ->
                if (FeedConstants.PACKAGE_FILE_NAME_FILTER.accept(artifact.path)) {
                    result[artifact] = path
                }
            }
        }
        return result
    }

    companion object {
        private val LOG = Logger.getInstance(NugetPackageIndexer::class.java.name)
    }
}
