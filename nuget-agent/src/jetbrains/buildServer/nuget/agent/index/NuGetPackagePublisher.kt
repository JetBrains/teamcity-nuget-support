package jetbrains.buildServer.nuget.agent.index

import jetbrains.buildServer.ArtifactsConstants
import jetbrains.buildServer.agent.ArtifactsPublisher
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.nuget.common.index.NuGetPackagesList
import jetbrains.buildServer.nuget.common.index.NuGetPackagesUtil
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.util.*

class NuGetPackagePublisher(private val publisher: ArtifactsPublisher) {
    fun publishPackages(packages: ArrayList<NuGetPackageData>) {
        if (packages.size == 0) return

        var tempDir: File? = null
        try {
            tempDir = FileUtil.createTempDirectory("packages", "list")
            val tempFile = File(tempDir, PackageConstants.PACKAGES_LIST_NAME)

            tempFile.outputStream().use {
                NuGetPackagesUtil.writePackages(NuGetPackagesList(packages), it)
            }

            val newArtifacts = HashMap<File, String>()
            newArtifacts[tempFile] = NUGET_PACKAGES_DIR
            publisher.publishFiles(newArtifacts)
        } finally {
            if (tempDir != null) {
                FileUtil.delete(tempDir)
            }
        }
    }

    companion object {
        const val NUGET_PACKAGES_DIR = ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + "/" +
            PackageConstants.NUGET_PROVIDER_ID
    }
}
