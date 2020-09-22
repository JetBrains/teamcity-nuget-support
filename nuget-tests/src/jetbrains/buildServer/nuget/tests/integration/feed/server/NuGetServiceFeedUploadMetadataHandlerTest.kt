package jetbrains.buildServer.nuget.tests.integration.feed.server

import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload.NuGetServiceFeedUploadHandlerContext
import jetbrains.buildServer.nuget.feed.server.controllers.serviceFeed.upload.NuGetServiceFeedUploadMetadataHandlerImpl
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProvider
import jetbrains.buildServer.serverSide.RunningBuildEx
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import org.jmock.Expectations
import org.jmock.Mockery
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import javax.servlet.http.HttpServletResponse

@Test
class NuGetServiceFeedUploadMetadataHandlerTest {
    private var myIsPersonal: Boolean = false
    private var myBuildId: Long = 123L
    private lateinit var myFeedsProvider: NuGetBuildFeedsProvider
    private lateinit var myContext: NuGetServiceFeedUploadHandlerContext
    private lateinit var myResponse: HttpServletResponse
    private lateinit var myBuild: RunningBuildEx
    private lateinit var myRequest: MultipartHttpServletRequest
    private lateinit var myStorage: MetadataStorage
    private lateinit var m : Mockery

    @DataProvider
    fun targetFeedsProvider(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(setOf<NuGetFeedData>()),
                arrayOf(setOf<NuGetFeedData>(NuGetFeedData("projectId1", "feedId1"))),
                arrayOf(setOf<NuGetFeedData>(
                        NuGetFeedData("projectId1", "feedId1"),
                        NuGetFeedData("projectId2", "feedId2")
                ))
        )
    }

    @BeforeMethod
    fun setupMethod() {
        m = Mockery()
        myStorage = m.mock(MetadataStorage::class.java)
        myRequest = m.mock(MultipartHttpServletRequest::class.java)
        myResponse = m.mock(HttpServletResponse::class.javaObjectType)
        myBuild = m.mock(RunningBuildEx::class.java)
        myContext = m.mock(NuGetServiceFeedUploadHandlerContext::class.java)
        myFeedsProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        myBuildId = 123L
        myIsPersonal = true

        m.checking(object: Expectations() {
            init {
                allowing(myBuild).buildId
                will(returnValue(myBuildId))

                allowing(myBuild).buildNumber
                will(returnValue("Build Number"))

                allowing(myBuild).buildTypeExternalId
                will(returnValue("Build TYpe External Id"))

                allowing(myBuild).isPersonal
                will(returnValue(myIsPersonal))
            }
        })
    }

    @Test(dataProvider = "targetFeedsProvider")
    fun shouldHandleMetadata(targetFeeds : Set<NuGetFeedData>) {
        // Given
        val instance = createInstance()
        val key = "packageId"
        val metadata = HashMap<String, String>()

        m.checking(object: Expectations() {
            init {
                oneOf(myFeedsProvider).getFeeds(myBuild)
                will(returnValue(targetFeeds))

                for(feed in targetFeeds) {
                    oneOf(myStorage).addBuildEntry(myBuildId, "nuget.${feed.projectId}.${feed.feedId}", key, metadata, !myIsPersonal)
                }
            }
        })


        // When
        instance.handleMetadata(myRequest , myResponse, myContext, myBuild, key, metadata)

        // Then
        m.assertIsSatisfied()
    }

    private fun createInstance() : NuGetServiceFeedUploadMetadataHandlerImpl {
        return NuGetServiceFeedUploadMetadataHandlerImpl(myStorage, myFeedsProvider)
    }
}
