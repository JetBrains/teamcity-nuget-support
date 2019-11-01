package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.common.FeedConstants
import jetbrains.buildServer.nuget.common.PackageLoadException
import jetbrains.buildServer.nuget.common.index.*
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.ServerResponsibility
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.serverSide.impl.LogUtil
import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.IOException
import java.util.*

class NuGetBuildMetadataProviderImpl(private val myPackageAnalyzer: PackageAnalyzer,
                                     private val myServerResponsibility: ServerResponsibility) : NuGetBuildMetadataProvider {

    override fun getPackagesMetadata(build: SBuild): Metadata {
        var metadata: Metadata = readBuildMetadata(build)
        when (metadata.state) {
            MetadataState.IsAbsent -> metadata = Metadata(MetadataState.Synchronized, indexBuildPackages(build))
            MetadataState.Unsynchronized -> metadata = Metadata(MetadataState.Unsynchronized, indexBuildPackages(build))
            MetadataState.Synchronized -> return metadata
        }

        if (myServerResponsibility.isResponsibleForBuild(build)) {
            try {
                writeBuildMetadata(build, metadata)
            } catch (e: Throwable) {
                LOG.warnAndDebugDetails("Failed to write packages list for build ${LogUtil.describe(build)}", e)
            }
        }

        return metadata
    }

    private fun indexBuildPackages(build: SBuild): Collection<NuGetPackageData> {
        val nugetArtifacts = HashSet<BuildArtifact>()
        val artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

        visitArtifacts(artifacts.rootArtifact, nugetArtifacts)

        return nugetArtifacts.mapNotNull {
            LOG.info("Indexing NuGet package from artifact ${it.relativePath} of build ${LogUtil.describe(build)}")
            try {
                generateMetadataForPackage(build, it)
            } catch (e: PackageLoadException) {
                LOG.warnAndDebugDetails("Failed to read NuGet package $it", e)
                null
            } catch (e: Exception) {
                LOG.warnAndDebugDetails("Unexpected error while indexing NuGet package $it", e)
                null
            }
        }
    }

    private fun writeBuildMetadata(build: SBuild, metadata: Metadata) {
        if (metadata.packages.isNotEmpty()) {
            val packages = metadata.packages.mapNotNull {
                it.metadata[PackageConstants.TEAMCITY_ARTIFACT_RELPATH]?.let { path ->
                    NuGetPackageData(path, it.metadata)
                }
            }

            //Thread.sleep(20000)

            val packageFile = File(build.artifactsDirectory, PackageConstants.PACKAGES_FILE_PATH)
            FileUtil.createDir(packageFile.parentFile)

            LOG.debug("Writing list of NuGet packages into $packageFile file")
            packageFile.outputStream().use {
                NuGetPackagesUtil.writePackages(NuGetPackagesList(packages), it)
            }
        }
    }

    private fun readBuildMetadata(build: SBuild): Metadata {
        val artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
        val tempPackagesArtifact = artifacts.getArtifact(PackageConstants.TEMP_PACKAGES_FILE_PATH)
        if (tempPackagesArtifact != null) {
            if (myServerResponsibility.isResponsibleForBuild(build)) {
                LOG.debug("Renaming temporary packages.json file to reqular one")
                deletePackageFile(build)
                FileUtil.renameFileNameOnly(File(build.artifactsDirectory, PackageConstants.TEMP_PACKAGES_FILE_PATH), PackageConstants.PACKAGES_LIST_NAME)
            }
        }
        val artifact = artifacts.getArtifact(PackageConstants.PACKAGES_FILE_PATH)
        if (artifact != null) {
            try {
                val metadata = artifact.inputStream.use {
                    NuGetPackagesUtil
                            .readPackages(it)
                            ?.let {
                                list -> list.packages.map {
                                    packageInfo -> NuGetPackageData(packageInfo.key, packageInfo.value )
                                }
                            }
                            ?.let { packages ->
                                Metadata(MetadataState.Synchronized, packages)
                            }
                            ?: Metadata(MetadataState.Synchronized)
                }

                val hasAnyUnavailablePackages =
                        metadata.packages.asSequence()
                        .map { pack -> pack.metadata[PackageConstants.TEAMCITY_ARTIFACT_RELPATH] }
                        .filterNotNull()
                        .filter {
                            val packageArtifact = artifacts.findArtifact(it)
                            LOG.debug("Checking artifact by path: '${packageArtifact.relativePath}', isAvailable: ${packageArtifact.isAvailable}, isAccessible: ${packageArtifact.isAccessible}")
                            !packageArtifact.isAvailable || !packageArtifact.isAccessible
                        }
                        .any()

                if (hasAnyUnavailablePackages) {
                    deletePackageFile(build)
                    return Metadata(MetadataState.Unsynchronized)
                }

                return metadata
            } catch (e: Exception) {
                LOG.warnAndDebugDetails("Failed to read NuGet packages list for build ${LogUtil.describe(build)}", e)
            }

            deletePackageFile(build)
        }

        return Metadata(MetadataState.IsAbsent)
    }

    private fun deletePackageFile(build: SBuild) {
        if (myServerResponsibility.isResponsibleForBuild(build)) {
            FileUtil.delete(File(build.artifactsDirectory, PackageConstants.PACKAGES_FILE_PATH))
        }
    }

    private fun generateMetadataForPackage(build: SBuild, artifact: BuildArtifact): NuGetPackageData {
        val metadata = try {
            artifact.inputStream.use {
                myPackageAnalyzer.analyzePackage(it)
            }
        } catch (e: IOException) {
            throw PackageLoadException("Failed to read build artifact data", e)
        }

        metadata[NuGetPackageAttributes.PACKAGE_SIZE] = artifact.size.toString()
        metadata[PackageConstants.TEAMCITY_ARTIFACT_RELPATH] = artifact.relativePath
        metadata[PackageConstants.TEAMCITY_BUILD_TYPE_ID] = build.buildTypeId

        try {
            artifact.inputStream.use {
                metadata[NuGetPackageAttributes.PACKAGE_HASH] = myPackageAnalyzer.getSha512Hash(it)
                metadata[NuGetPackageAttributes.PACKAGE_HASH_ALGORITHM] = PackageAnalyzer.SHA512
            }
        } catch (e: IOException) {
            throw PackageLoadException("Failed to calculate package hash", e)
        }

        val finishDate = build.finishDate
        val created = ODataDataFormat.formatDate(finishDate ?: Date())
        metadata[NuGetPackageAttributes.CREATED] = created
        metadata[NuGetPackageAttributes.LAST_UPDATED] = created
        metadata[NuGetPackageAttributes.PUBLISHED] = created

        return NuGetPackageData(artifact.relativePath, metadata)
    }

    private fun visitArtifacts(artifact: BuildArtifact, packages: MutableSet<BuildArtifact>) {
        if (!artifact.isDirectory) {
            if (FeedConstants.PACKAGE_FILE_NAME_FILTER.accept(artifact.name)) {
                packages.add(artifact)
            }
            return
        }

        for (children in artifact.children) {
            visitArtifacts(children, packages)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetBuildMetadataProviderImpl::class.java.name)
    }
}
