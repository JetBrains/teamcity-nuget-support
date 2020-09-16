package jetbrains.buildServer.nuget.feed.server.controllers

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.controllers.upload.PackageUploadHandler
import jetbrains.buildServer.web.CsrfCheck
import javax.servlet.http.HttpServletRequest

/**
 * Marks request from nuget delete/push commands safe.
 */
class NuGetCsrfCheck : CsrfCheck {

    override fun describe(verbose: Boolean) = "NuGet feed CSRF check"

    override fun isSafe(request: HttpServletRequest): CsrfCheck.CheckResult {
        if (!ACTION_METHODS.contains(request.method)) {
            return CsrfCheck.UNKNOWN
        }

        if (!request.requestURL.contains(NuGetServerSettings.PATH_PREFIX)) {
            CsrfCheck.UNKNOWN
        }

        if (request.getHeader(PackageUploadHandler.NUGET_APIKEY_HEADER).isNullOrEmpty()) {
            return CsrfCheck.UNKNOWN
        }

        return CsrfCheck.CheckResult.safe()
    }

    companion object {
        val ACTION_METHODS = setOf("PUT")
    }
}
