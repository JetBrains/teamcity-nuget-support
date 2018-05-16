package jetbrains.buildServer.nuget.common.index

class NuGetPackagesList(packages: List<NuGetPackageData>) {
    val version = "2018.1"
    val packages = packages.associateBy({ it.path }, { it.metadata })
}
