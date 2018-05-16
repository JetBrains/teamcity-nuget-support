package jetbrains.buildServer.nuget.agent.index

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsBuilderAdapter
import jetbrains.buildServer.agent.impl.artifacts.ArtifactsCollection
import jetbrains.buildServer.nuget.common.FeedConstants
import jetbrains.buildServer.nuget.common.NuGetServerConstants
import jetbrains.buildServer.nuget.common.index.ODataDataFormat
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_ARTIFACT_RELPATH
import jetbrains.buildServer.nuget.common.index.PackageConstants.TEAMCITY_BUILD_TYPE_ID
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*
import jetbrains.buildServer.util.EventDispatcher
import java.io.File
import java.util.*

class NugetPackageIndexer(dispatcher: EventDispatcher<AgentLifeCycleListener>,
                          private val packageAnalyzer: PackageAnalyzer)
    : ArtifactsBuilderAdapter() {

    private var myIndexingEnabled = false
    private var myBuildType: String? = null

    init {
        dispatcher.addListener(object : AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                super.buildStarted(runningBuild)
                val parameters = runningBuild.sharedBuildParameters.allParameters
                myIndexingEnabled = parameters[NuGetServerConstants.FEED_INDEXING_ENABLED_PROP]?.toBoolean() ?: false
                myBuildType = runningBuild.buildTypeId
            }
        })
    }

    override fun afterCollectingFiles(artifacts: MutableList<ArtifactsCollection>) {
        super.afterCollectingFiles(artifacts)
        getPackages(artifacts).forEach { (file, path) ->
            val metadata = file.inputStream().use {
                val metadata = packageAnalyzer.analyzePackage(it)
                it.reset()
                metadata[PACKAGE_HASH] = packageAnalyzer.getSha512Hash(it)
                metadata[PACKAGE_HASH_ALGORITHM] = PackageAnalyzer.SHA512
                metadata
            }

            val created = ODataDataFormat.formatDate(Date())
            metadata[CREATED] = created
            metadata[LAST_UPDATED] = created
            metadata[PUBLISHED] = created
            metadata[PACKAGE_SIZE] = file.length().toString()
            metadata[TEAMCITY_ARTIFACT_RELPATH] = "$path/${file.name}"
            myBuildType?.let {
                metadata.put(TEAMCITY_BUILD_TYPE_ID, it)
            }
        }
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
}
