package jetbrains.buildServer.nuget.common.index

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import java.io.*

object NuGetPackagesUtil {

    @JvmStatic
    fun writePackages(packages: NuGetPackagesList, stream: OutputStream) {
        try {
            stream.bufferedWriter().use {
                it.write(gson.toJson(packages))
            }
        } catch (e: IOException) {
            LOG.warnAndDebugDetails("Failed to write NuGet packages list", e)
        }
    }

    @JvmStatic
    fun readPackages(stream: InputStream): NuGetPackagesList? {
        return try {
            stream.bufferedReader().use {
                gson.fromJson(it, NuGetPackagesList::class.java)
            }
        } catch (e: IOException) {
            LOG.warnAndDebugDetails("Failed to read NuGet packages list", e)
            null
        }
    }

    private val LOG: Logger = Logger.getInstance(NuGetPackagesUtil::class.java.name)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
}
