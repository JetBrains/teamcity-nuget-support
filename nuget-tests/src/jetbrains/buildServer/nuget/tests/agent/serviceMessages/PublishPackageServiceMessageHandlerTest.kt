package jetbrains.buildServer.nuget.tests.agent.serviceMessages

import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage.SERVICE_MESSAGE_END
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage.SERVICE_MESSAGE_START
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister
import jetbrains.buildServer.nuget.agent.serviceMessages.NuGetPackageServiceFeedPublisher
import jetbrains.buildServer.nuget.agent.serviceMessages.PublishPackageServiceMessageHandler
import jetbrains.buildServer.nuget.common.PackageLoadException
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.FileUtil
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Paths

@Test
class PublishPackageServiceMessageHandlerTest : BaseTestCase() {
    private lateinit var myTempArtifactsDir: File
    private lateinit var myLogger: BuildProgressLogger
    private lateinit var m: Mockery
    private lateinit var myPackagePublisher: NuGetPackageServiceFeedPublisher
    private lateinit var myPackageAnalyzer: PackageAnalyzer
    private lateinit var myDispatcher: EventDispatcher<AgentLifeCycleListener>
    private lateinit var myServiceMessagesRegister: ServiceMessagesRegister
    private lateinit var myBuild: AgentRunningBuild

    @DataProvider
    fun buildFinishStatusesProvider(): Array<Array<out Any?>> {
        return BuildFinishedStatus.values().map { arrayOf(it) }.toTypedArray()
    }

    @DataProvider
    fun buildFinishFailedStatusesProvider(): Array<Array<out Any?>> {
        return BuildFinishedStatus.values().filter{ it != BuildFinishedStatus.FINISHED_SUCCESS }.map { arrayOf(it) }.toTypedArray()
    }

    @BeforeMethod
    fun setupMethod() {
        m = Mockery()
        myPackagePublisher = m.mock(NuGetPackageServiceFeedPublisher::class.java)
        myPackageAnalyzer = m.mock(PackageAnalyzer::class.java)
        myDispatcher = AgentEventDispatcher()
        myServiceMessagesRegister = m.mock(ServiceMessagesRegister::class.java)
        myBuild = m.mock(AgentRunningBuild::class.java)
        myLogger = m.mock(BuildProgressLogger::class.java)

        myTempArtifactsDir = createTempDir()
        FileUtil.copyDir(Paths.get("testData/packages").toFile(), myTempArtifactsDir)

        m.checking(object: Expectations() {
            init {
                oneOf(myServiceMessagesRegister).registerHandler(with(equal(MESSAGE_NAME)), with(any(PublishPackageServiceMessageHandler::class.java)))

                allowing(myBuild).buildLogger
                will(returnValue(myLogger))
            }
        })
    }

    @Test
    fun shouldDispose() {
        // Given
        val instance = createInstance()

        m.checking(object: Expectations() {
            init {
                oneOf(myServiceMessagesRegister).removeHandler(with(equal(MESSAGE_NAME)))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)

        // When
        instance.dispose()

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldPublishPackage() {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val filePath = "CommonServiceLocator.1.0.nupkg"
        val metadata = mapOf<String, String>(
                NuGetPackageAttributes.ID to filePath,
                NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
        )

        m.checking(object: Expectations() {
            init {
                oneOf(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                oneOf(myPackageAnalyzer).analyzePackage(with(any(InputStream::class.java)))
                will(returnValue(metadata))

                oneOf(myPackagePublisher).publishPackages(withNotNull(createPackagesMatcher(listOf(File(checkoutDirectory, filePath).path)), emptyList()))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '$filePath'$SERVICE_MESSAGE_END")!!)
        myDispatcher.multicaster.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        // Then
        m.assertIsSatisfied()
    }

    @Test(dataProvider = "buildFinishFailedStatusesProvider")
    fun shouldNotPublishPackageForFailedStatuses(buildFinishStatus: BuildFinishedStatus) {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val filePath = "CommonServiceLocator.1.0.nupkg"
        val metadata = mapOf<String, String>(
                NuGetPackageAttributes.ID to filePath,
                NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
        )

        m.checking(object: Expectations() {
            init {
                oneOf(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                oneOf(myPackageAnalyzer).analyzePackage(with(any(InputStream::class.java)))
                will(returnValue(metadata))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '$filePath'$SERVICE_MESSAGE_END")!!)
        myDispatcher.multicaster.runnerFinished(runner, buildFinishStatus)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldPublishSeveralPackages() {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val packages = listOf(
                NuGetPackageData("CommonServiceLocator.1.0.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "CommonServiceLocator.1.0.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
                )),
                NuGetPackageData("dependencyGroup.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "dependencyGroup.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "2.0"
                ))
        )

        val analyzePackageSequence = m.sequence("analyzePackage")
        m.checking(object: Expectations() {
            init {
                allowing(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                for(pkg in packages) {
                    oneOf(myPackageAnalyzer).analyzePackage(with(any(InputStream::class.java)))
                    inSequence(analyzePackageSequence)
                    will(returnValue(pkg.metadata))
                }

                oneOf(myPackagePublisher).publishPackages(withNotNull(
                        createPackagesMatcher(
                                packages.map { File(checkoutDirectory, it.path).path }.toList()
                        ),
                        emptyList()))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        for(pkg in packages) {
            instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '${pkg.path}'$SERVICE_MESSAGE_END")!!)
        }
        myDispatcher.multicaster.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldGroupSamePackages() {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val packages = listOf(
                NuGetPackageData("CommonServiceLocator.1.0.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "CommonServiceLocator.1.0.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
                )),
                NuGetPackageData("dependencyGroup.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "dependencyGroup.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "2.0"
                )),
                NuGetPackageData("CommonServiceLocator.1.0.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "CommonServiceLocator.1.0.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
                )),
                NuGetPackageData("dependencyGroup.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "dependencyGroup.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "2.0"
                ))
        )

        val analyzePackageSequence = m.sequence("analyzePackage")
        m.checking(object: Expectations() {
            init {
                allowing(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                for(pkg in packages) {
                    oneOf(myPackageAnalyzer).analyzePackage(with(any(InputStream::class.java)))
                    inSequence(analyzePackageSequence)
                    will(returnValue(pkg.metadata))
                }

                oneOf(myPackagePublisher).publishPackages(withNotNull(
                        createPackagesMatcher(
                                packages.map { File(checkoutDirectory, it.path).path }.distinct().toList()
                        ),
                        emptyList()))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        for(pkg in packages) {
            instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '${pkg.path}'$SERVICE_MESSAGE_END")!!)
        }
        myDispatcher.multicaster.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldLogErrorOnPackageLoadException() {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val packages = listOf(
                NuGetPackageData("CommonServiceLocator.1.0.nupkg", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "CommonServiceLocator.1.0.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
                ))
        )

        val analyzePackageSequence = m.sequence("analyzePackage")
        m.checking(object: Expectations() {
            init {
                oneOf(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                for(pkg in packages) {
                    oneOf(myPackageAnalyzer).analyzePackage(with(any(InputStream::class.java)))
                    inSequence(analyzePackageSequence)
                    will(throwException(PackageLoadException("Test Error")))
                }

                oneOf(myLogger).exception(with(createExceptionMatcher<PackageLoadException>("Test Error")))
                oneOf(myLogger).buildFailureDescription("Could not read NuGet package")
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        for(pkg in packages) {
            instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '${pkg.path}'$SERVICE_MESSAGE_END")!!)
        }
        myDispatcher.multicaster.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        // Then
        m.assertIsSatisfied()
    }

    @Test
    fun shouldLogErrorOnException() {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val packages = listOf(
                NuGetPackageData("CommonServiceLocator.1.0.nupkg-1", mapOf<String, String>(
                        NuGetPackageAttributes.ID to "CommonServiceLocator.1.0.nupkg",
                        NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
                ))
        )

        val analyzePackageSequence = m.sequence("analyzePackage")
        m.checking(object: Expectations() {
            init {
                oneOf(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                oneOf(myLogger).exception(with(any(FileNotFoundException::class.java)))
                oneOf(myLogger).buildFailureDescription("Could not read NuGet package")
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        for(pkg in packages) {
            instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '${pkg.path}'$SERVICE_MESSAGE_END")!!)
        }
        myDispatcher.multicaster.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        // Then
        m.assertIsSatisfied()
    }

    @Test(dataProvider = "buildFinishStatusesProvider")
    fun shouldClearPackagesListOnRunnerFinished(buildFinishStatus: BuildFinishedStatus) {
        // Given
        val instance = createInstance()
        val runner = m.mock(BuildRunnerContext::class.java)
        val checkoutDirectory = myTempArtifactsDir
        val filePath = "CommonServiceLocator.1.0.nupkg"
        val metadata = mapOf<String, String>(
                NuGetPackageAttributes.ID to filePath,
                NuGetPackageAttributes.NORMALIZED_VERSION to "1.0"
        )

        m.checking(object: Expectations() {
            init {
                oneOf(myBuild).checkoutDirectory
                will(returnValue(checkoutDirectory))

                oneOf(myPackageAnalyzer).analyzePackage(with(any(InputStream::class.java)))
                will(returnValue(metadata))

                allowing(myPackagePublisher).publishPackages(withNotNull(createPackagesMatcher(listOf(File(checkoutDirectory, filePath).path)), emptyList()))
            }
        })

        myDispatcher.multicaster.buildStarted(myBuild)
        myDispatcher.multicaster.beforeRunnerStart(runner)

        // When
        instance.handle(ServiceMessage.parse("$SERVICE_MESSAGE_START$MESSAGE_NAME '$filePath'$SERVICE_MESSAGE_END")!!)
        myDispatcher.multicaster.runnerFinished(runner, buildFinishStatus)

        myDispatcher.multicaster.beforeRunnerStart(runner)
        myDispatcher.multicaster.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        // Then
        m.assertIsSatisfied()
    }

    private fun createInstance() : PublishPackageServiceMessageHandler {
        return PublishPackageServiceMessageHandler(myServiceMessagesRegister, myDispatcher, myPackageAnalyzer, myPackagePublisher)
    }

    private fun createPackagesMatcher(packages: Collection<String>): Matcher<Collection<NuGetPackageData>> {
        return object: BaseMatcher<Collection<NuGetPackageData>>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Expecting files: " + packages.joinToString(","))
            }

            override fun matches(o: Any?): Boolean {
                @Suppress("UNCHECKED_CAST")
                val value = o as Collection<NuGetPackageData>?
                if (value == null) return false;

                return value.map { it.path }.equals(packages)
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

    private fun<T> Expectations.withNotNull(equal: Matcher<T>, value: T): T {
        with(equal)
        return value
    }

    private companion object {
        const val MESSAGE_NAME = "publishNuGetPackage"
        const val MESSAGE_PATH_ATTRIBUTE = "path"
    }
}
