package jetbrains.buildServer.nuget.tests.feed

import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.ProjectAwareRootUrlResolver
import jetbrains.buildServer.nuget.feed.server.impl.NuGetFeedRootUrlResolver
import jetbrains.buildServer.nuget.tests.integration.feed.server.RequestWrapper
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils
import jetbrains.buildServer.web.util.PathModifiers
import jetbrains.buildServer.web.util.WebUtil
import org.assertj.core.api.Assertions
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import javax.servlet.http.HttpServletRequest

class NuGetFeedRootUrlResolverTest : BaseTestCase() {

    private lateinit var myMockery: Mockery
    private lateinit var myProjectAwareResolver: ProjectAwareRootUrlResolver
    private lateinit var myResolver: NuGetFeedRootUrlResolver

    @BeforeMethod
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myMockery = TCJMockUtils.createInstance()
        myProjectAwareResolver = myMockery.mock(ProjectAwareRootUrlResolver::class.java)
        myResolver = NuGetFeedRootUrlResolver(myProjectAwareResolver)
        registerHttpAuthPathModifier()
    }

    @Test
    fun testRequestUrlIsUsedWhenProjectDoesNotRedefineRootUrl() {
        expectProjectRootUrl(PROJECT_EXT_ID, GLOBAL_URL)

        Assertions.assertThat(myResolver.getRootUrl(createRequest(), PROJECT_EXT_ID)).isEqualTo(REQUEST_URL)
        Assertions.assertThat(myResolver.getRootUrlWithAuthenticationType(createRequest(), PROJECT_EXT_ID))
            .isEqualTo("$REQUEST_URL/httpAuth")
    }

    @Test
    fun testProjectUrlOverridesRequestUrl() {
        expectProjectRootUrl(PROJECT_EXT_ID, PROJECT_URL)

        Assertions.assertThat(myResolver.getRootUrl(createRequest(), PROJECT_EXT_ID)).isEqualTo(PROJECT_URL)
        Assertions.assertThat(myResolver.getRootUrlWithAuthenticationType(createRequest(), PROJECT_EXT_ID))
            .isEqualTo("$PROJECT_URL/httpAuth")
    }

    @Test
    fun testTrailingSlashOfProjectUrlIsRemoved() {
        expectProjectRootUrl(PROJECT_EXT_ID, "$PROJECT_URL/")

        Assertions.assertThat(myResolver.getRootUrl(createRequest(), PROJECT_EXT_ID)).isEqualTo(PROJECT_URL)
        Assertions.assertThat(myResolver.getRootUrlWithAuthenticationType(createRequest(), PROJECT_EXT_ID))
            .isEqualTo("$PROJECT_URL/httpAuth")
    }

    @Test
    fun testUnknownProjectFallsBackToRequestUrl() {
        expectProjectRootUrl(null, GLOBAL_URL)

        Assertions.assertThat(myResolver.getRootUrl(createRequest(), null)).isEqualTo(REQUEST_URL)
        Assertions.assertThat(myResolver.getRootUrlWithAuthenticationType(createRequest(), null))
            .isEqualTo("$REQUEST_URL/httpAuth")
    }

    private fun expectProjectRootUrl(projectExtId: String?, projectRootUrl: String) {
        myMockery.checking(object : Expectations() {
            init {
                allowing(myProjectAwareResolver).rootUrl
                will(returnValue(GLOBAL_URL))
                allowing(myProjectAwareResolver).getRootUrlByProjectExternalId(projectExtId)
                will(returnValue(projectRootUrl))
            }
        })
    }

    private fun createRequest(): HttpServletRequest {
        val request = RequestWrapper(SERVLET_PATH, "$SERVLET_PATH/index.json")
        request.setServerPort(8111)
        return request
    }

    private fun registerHttpAuthPathModifier() {
        val pathModifiers = PathModifiers()
        pathModifiers.init()
        pathModifiers.registerPathModifier(object : PathModifiers.PathModifier {
            override fun matches(path: String) = path.startsWith(WebUtil.HTTP_AUTH_PREFIX)
            override fun modifyPath(path: String) = path.substring(WebUtil.HTTP_AUTH_PREFIX.length - 1)
        })
    }

    companion object {
        private const val PROJECT_EXT_ID = "Project1"
        private const val GLOBAL_URL = "http://teamcity.example.com"
        private const val PROJECT_URL = "https://tenant.example.com"
        private const val REQUEST_URL = "http://localhost:8111"
        private const val SERVLET_PATH = "/httpAuth/app/nuget/feed/Project1/default/v3"
    }
}
