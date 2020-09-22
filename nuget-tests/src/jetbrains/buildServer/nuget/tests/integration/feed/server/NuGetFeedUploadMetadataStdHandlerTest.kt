package jetbrains.buildServer.nuget.tests.integration.feed.server

import jetbrains.buildServer.nuget.common.PackageExistsException
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadHandlerStdContext
import jetbrains.buildServer.nuget.feed.server.controllers.upload.NuGetFeedUploadMetadataStdHandlerImpl
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.RunningBuildEx
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import org.hamcrest.collection.IsEmptyCollection
import org.jmock.Expectations
import org.jmock.Mockery
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import javax.servlet.http.HttpServletResponse

@Test
class NuGetFeedUploadMetadataStdHandlerTest {
    private lateinit var myContext: NuGetFeedUploadHandlerStdContext
    private lateinit var myResponse: HttpServletResponse
    private lateinit var myBuild: RunningBuildEx
    private lateinit var myRequest: MultipartHttpServletRequest
    private lateinit var myStorage: MetadataStorage
    private lateinit var m : Mockery

    @DataProvider
    fun replaceDataProvider(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("true"),
                arrayOf("false")
        )
    }

    @BeforeMethod
    fun setupMethod() {
        m = Mockery()
        myStorage = m.mock(MetadataStorage::class.java)
        myRequest = m.mock(MultipartHttpServletRequest::class.java)
        myResponse = m.mock(HttpServletResponse::class.javaObjectType)
        myBuild = m.mock(RunningBuildEx::class.java)
        myContext = m.mock(NuGetFeedUploadHandlerStdContext::class.java)

        m.checking(object: Expectations() {
            init {
                allowing(myContext).feedData;
                will(returnValue(NuGetFeedData(PROJECT_ID, FEED_ID)))
            }
        })
    }

    @Test(dataProvider = "replaceDataProvider")
    fun shouldNotThrowWhenValidateUniquePackage(replace: String) {
        // Given
        val instance = createInstance()
        val key = "packageId"
        val metadata = HashMap<String, String>()

        m.checking(object: Expectations() {
            init {
                oneOf(myRequest).getParameter("replace")
                will(returnValue(replace))

                allowing(myStorage).getEntriesByKey("nuget.$PROJECT_ID.$FEED_ID", key)
                will(returnValue(emptyList<BuildMetadataEntry>().iterator()))
            }
        })

        // When
        instance.validate(myRequest , myResponse, myContext, myBuild, key, metadata)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldNotThrowWhenValidateAndReplaceExistingPackage() {
        // Given
        val instance = createInstance()
        val key = "packageId"
        val metadata = HashMap<String, String>()

        m.checking(object: Expectations() {
            init {
                oneOf(myRequest).getParameter("replace")
                will(returnValue("true"))

                allowing(myStorage).getEntriesByKey("nuget.$PROJECT_ID.$FEED_ID", key)
                will(returnValue(listOf<BuildMetadataEntry>(m.mock(BuildMetadataEntry::class.java)).iterator()))
            }
        })


        // When
        instance.validate(myRequest , myResponse, myContext, myBuild, key, metadata)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldThrowWhenValidate() {
        // Given
        val instance = createInstance()
        val key = "packageId"
        val metadata = HashMap<String, String>()

        m.checking(object: Expectations() {
            init {
                oneOf(myRequest).getParameter("replace")
                will(returnValue("false"))

                allowing(myStorage).getEntriesByKey("nuget.$PROJECT_ID.$FEED_ID", key)
                will(returnValue(listOf<BuildMetadataEntry>(m.mock(BuildMetadataEntry::class.java)).iterator()))
            }
        })

        // When
        val action = { instance.validate(myRequest , myResponse, myContext, myBuild, key, metadata) }

        // Then
        Assert.assertThrows(PackageExistsException::class.java, action)
        m.assertIsSatisfied()
    }

    @Test
    fun shouldHandleMetadata() {
        // Given
        val instance = createInstance()
        val key = "packageId"
        val buildId = 123L
        val isPersonal = true
        val metadata = HashMap<String, String>()

        m.checking(object: Expectations() {
            init {
                oneOf(myBuild).buildId
                will(returnValue(buildId))

                oneOf(myBuild).isPersonal
                will(returnValue(isPersonal))

                oneOf(myStorage).addBuildEntry(buildId, "nuget.$PROJECT_ID.$FEED_ID", key, metadata, !isPersonal)
            }
        })


        // When
        instance.handleMetadata(myRequest , myResponse, myContext, myBuild, key, metadata)

        // Then
        m.assertIsSatisfied()
    }

    private fun createInstance() : NuGetFeedUploadMetadataStdHandlerImpl {
        return NuGetFeedUploadMetadataStdHandlerImpl(myStorage)
    }

    private companion object {
        const val FEED_ID = "feedId"
        const val PROJECT_ID = "projectId"
    }
}
