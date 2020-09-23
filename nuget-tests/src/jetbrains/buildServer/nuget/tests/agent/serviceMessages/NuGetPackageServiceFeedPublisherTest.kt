package jetbrains.buildServer.nuget.tests.agent.serviceMessages

import jetbrains.buildServer.BuildAuthUtil
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.FlowLogger
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.messages.BlockData
import jetbrains.buildServer.messages.BuildMessage1
import jetbrains.buildServer.nuget.agent.serviceMessages.NuGetPackageServiceFeedPublisherImpl
import jetbrains.buildServer.nuget.agent.serviceMessages.NuGetPackageServiceFeedResponse
import jetbrains.buildServer.nuget.agent.serviceMessages.NuGetPackageServiceFeedTransport
import jetbrains.buildServer.nuget.agent.serviceMessages.NuGetPackageServiceFeedTransportProvider
import jetbrains.buildServer.nuget.common.PackageLoadException
import jetbrains.buildServer.nuget.common.PackagePublishException
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.util.EventDispatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.IOException

@Test
class NuGetPackageServiceFeedPublisherTest {
    private lateinit var myApiKey: String
    private lateinit var myFeedTransport: NuGetPackageServiceFeedTransport
    private lateinit var myFeedTransportProvider: NuGetPackageServiceFeedTransportProvider
    private lateinit var myDispatcher: EventDispatcher<AgentLifeCycleListener>
    private lateinit var myBuild: AgentRunningBuild
    private lateinit var m: Mockery

    @BeforeMethod
    fun setupMethod() {
        m = Mockery()
        myDispatcher = AgentEventDispatcher()
        myFeedTransportProvider = m.mock(NuGetPackageServiceFeedTransportProvider::class.java)
        myBuild = m.mock(AgentRunningBuild::class.java)
        myFeedTransport = m.mock(NuGetPackageServiceFeedTransport::class.java)

        val buildId = 123L
        val accessCode = "AccessCode"
        myApiKey = createApiKey(buildId, accessCode)

        m.checking(object: Expectations() {
            init {
                allowing(myBuild).buildId
                will(returnValue(buildId))

                allowing(myBuild).accessCode
                will(returnValue(accessCode))
            }
        })
    }

    @Test
    fun shouldPublish() {
        // Given
        val instance = createInstance()
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val transportResponse = m.mock(NuGetPackageServiceFeedResponse::class.java)
        val filePath = "testPackage.nupkg"

        m.checking(object: Expectations() {
            init {
                allowing(myFeedTransportProvider).createTransport(myBuild)
                will(returnValue(myFeedTransport))

                allowing(myBuild).buildLogger
                will(returnValue(buildLogger))

                allowing(buildLogger).getFlowLogger(FLOW_ID)
                will(returnValue(flowLogger))

                oneOf(flowLogger).message("Publishing $filePath package")

                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_START, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_END, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).disposeFlow()

                oneOf(myFeedTransport).sendPackage(withNotNull(equal(myApiKey), ""), withNotNull(createFileMatcher(filePath), File("x")))
                will(returnValue(transportResponse))

                oneOf(transportResponse).isSuccessful
                will(returnValue(true))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)

        // When
        instance.publishPackages(listOf(NuGetPackageData(filePath, emptyMap<String, String>())))

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldPublishSeveralPackages() {
        // Given
        val instance = createInstance()
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val transportResponse = m.mock(NuGetPackageServiceFeedResponse::class.java)
        val packages = listOf(
                NuGetPackageData("testPackage1.nupkg", emptyMap()),
                NuGetPackageData("testPackage2.nupkg", emptyMap()),
                NuGetPackageData("testPackage3.nupkg", emptyMap())
        )

        m.checking(object: Expectations() {
            init {
                allowing(myFeedTransportProvider).createTransport(myBuild)
                will(returnValue(myFeedTransport))

                allowing(myBuild).buildLogger
                will(returnValue(buildLogger))

                allowing(buildLogger).getFlowLogger(FLOW_ID)
                will(returnValue(flowLogger))

                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_START, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_END, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).disposeFlow()

                for(path in packages.map { it.path }) {
                    oneOf(flowLogger).message("Publishing $path package")
                    oneOf(myFeedTransport).sendPackage(withNotNull(equal(myApiKey), ""), withNotNull(createFileMatcher(path), File("x")))
                    will(returnValue(transportResponse))
                }

                exactly(packages.size).of(transportResponse).isSuccessful
                will(returnValue(true))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)

        // When
        instance.publishPackages(packages)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldLogErrorIfResponseIsNotSuccessful() {
        // Given
        val instance = createInstance()
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val transportResponse = m.mock(NuGetPackageServiceFeedResponse::class.java)
        val filePath = "testPackage.nupkg"
        val statusCode = 123
        val message = "Test Error"

        m.checking(object: Expectations() {
            init {
                allowing(myFeedTransportProvider).createTransport(myBuild)
                will(returnValue(myFeedTransport))

                allowing(myBuild).buildLogger
                will(returnValue(buildLogger))

                allowing(buildLogger).getFlowLogger(FLOW_ID)
                will(returnValue(flowLogger))

                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).message("Publishing $filePath package")

                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_START, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_END, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).exception(with(createExceptionMatcher<PackagePublishException>(
                        "Failed to publush NuGet package. Server returned StatusCode: $statusCode, Response: $message")))
                oneOf(flowLogger).buildFailureDescription("Failed to publish NuGet packages")
                oneOf(flowLogger).disposeFlow()

                oneOf(myFeedTransport).sendPackage(withNotNull(equal(myApiKey), ""), withNotNull(createFileMatcher(filePath), File("x")))
                will(returnValue(transportResponse))

                oneOf(transportResponse).isSuccessful
                will(returnValue(false))

                oneOf(transportResponse).statusCode
                will(returnValue(statusCode))

                oneOf(transportResponse).message
                will(returnValue(message))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)

        // When
        instance.publishPackages(listOf(NuGetPackageData(filePath, emptyMap<String, String>())))

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldLogErrorSendFileThrowsException() {
        // Given
        val instance = createInstance()
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val filePath = "testPackage.nupkg"

        m.checking(object: Expectations() {
            init {
                allowing(myFeedTransportProvider).createTransport(myBuild)
                will(returnValue(myFeedTransport))

                allowing(myBuild).buildLogger
                will(returnValue(buildLogger))

                allowing(buildLogger).getFlowLogger(FLOW_ID)
                will(returnValue(flowLogger))

                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).message("Publishing $filePath package")

                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_START, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_END, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).exception(with(createExceptionMatcher<PackageLoadException>("Test Error")))
                oneOf(flowLogger).buildFailureDescription("Failed to publish NuGet packages")
                oneOf(flowLogger).disposeFlow()

                oneOf(myFeedTransport).sendPackage(withNotNull(equal(myApiKey), ""), withNotNull(createFileMatcher(filePath), File("x")))
                will(throwException(PackageLoadException("Test Error")))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)

        // When
        instance.publishPackages(listOf(NuGetPackageData(filePath, emptyMap<String, String>())))

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldLogErrorCreateTransportThrowsException() {
        // Given
        val instance = createInstance()
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val filePath = "testPackage.nupkg"

        m.checking(object: Expectations() {
            init {
                allowing(myFeedTransportProvider).createTransport(myBuild)
                will(throwException(IOException("Test Error")))

                allowing(myBuild).buildLogger
                will(returnValue(buildLogger))

                allowing(buildLogger).getFlowLogger(FLOW_ID)
                will(returnValue(flowLogger))

                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_START, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).logMessage(with(createLogMessageMatcher(BLOCK_END, BLOCK_NAME, BLOCK_TYPE)))
                oneOf(flowLogger).exception(with(createExceptionMatcher<IOException>("Test Error")))
                oneOf(flowLogger).buildFailureDescription("Failed to publish NuGet packages")
                oneOf(flowLogger).disposeFlow()
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)

        // When
        instance.publishPackages(listOf(NuGetPackageData(filePath, emptyMap<String, String>())))

        // Then
        m.assertIsSatisfied()
    }

    private fun<T> Expectations.withNotNull(equal: Matcher<T>, value: T): T {
        with(equal)
        return value
    }

    private fun createInstance() : NuGetPackageServiceFeedPublisherImpl {
        return NuGetPackageServiceFeedPublisherImpl(myDispatcher, myFeedTransportProvider)
    }

    private fun createLogMessageMatcher(typeId: String, blockName: String, blockType: String): Matcher<BuildMessage1> {
        return object: BaseMatcher<BuildMessage1>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Expecting BuildMessage1 with type: $blockType and $blockName")
            }

            override fun matches(o: Any?): Boolean {
                val value = o as BuildMessage1?;
                if (value == null) return false;
                val blockData = value.value as BlockData?
                return value.typeId == typeId
                        && value.sourceId == "DefaultMessage"
                        && blockData?.blockName == BLOCK_NAME
                        && blockData.blockType == BLOCK_TYPE
            }

        }
    }

    private fun createFileMatcher(filePath: String): Matcher<File> {
        return object: BaseMatcher<File>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Expecting file: $filePath")
            }

            override fun matches(o: Any?): Boolean {
                val value = o as File?
                if (value == null) return false
                return value.path == filePath
            }
        }
    }

    private fun<E: Throwable> createExceptionMatcher(message: String): Matcher<E> {
        return object: BaseMatcher<E>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Expecting exception: $message")
            }

            override fun matches(o: Any?): Boolean {
                @Suppress("UNCHECKED_CAST")
                val value = o as E?

                if (value == null) return false
                return value.message == message
            }
        }
    }

    private fun createApiKey(buildId: Long, accessCode: String): String {
        val buildToken = String.format("%s:%s", BuildAuthUtil.makeUserId(buildId), accessCode)
        return EncryptUtil.scramble(buildToken)
    }

    private companion object {
        const val BLOCK_START = "BlockStart"
        const val BLOCK_END = "BlockEnd"
        const val BLOCK_TYPE = "publish-nuget-packages"
        const val BLOCK_NAME = "Publishing NuGet packages"
        const val FLOW_ID = "publish-nuget-packages-flow-id"
    }
}
