// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.utils.rules

import com.intellij.ide.util.projectWizard.EmptyModuleBuilder
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.ModuleFixture
import com.intellij.testFramework.fixtures.TestFixtureBuilder
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl
import com.jetbrains.python.PythonModuleTypeBase
import com.jetbrains.python.sdk.PythonSdkAdditionalData
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.flavors.CPythonSdkFlavor
import org.jetbrains.annotations.NotNull
import software.aws.toolkits.jetbrains.testutils.rules.CodeInsightTestFixtureRule

/**
 * JUnit test Rule that will create a Light [Project] and [CodeInsightTestFixture] with Python support. Projects are
 * lazily created and are torn down after each test.
 *
 * If you wish to have just a [Project], you may use Intellij's [com.intellij.testFramework.ProjectRule]
 */
class PythonCodeInsightTestFixtureRule : CodeInsightTestFixtureRule() {
    override fun createTestFixture(): CodeInsightTestFixture {
        val fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory()
        fixtureFactory.registerFixtureBuilder(
            PythonModuleFixtureBuilder::class.java,
            PythonModuleFixtureBuilder::class.java
        )
        val fixtureBuilder = fixtureFactory.createFixtureBuilder(testName)
        fixtureBuilder.addModule(PythonModuleFixtureBuilder::class.java)
        val newFixture = fixtureFactory.createCodeInsightFixture(fixtureBuilder.fixture)
        newFixture.testDataPath = testDataPath
        newFixture.setUp()

        val module = newFixture.module

        val projectRoot = newFixture.tempDirFixture.getFile(".")
        PsiTestUtil.addSourceRoot(module, projectRoot)
        PsiTestUtil.addContentRoot(module, projectRoot)

        ModuleRootModificationUtil.setModuleSdk(module, PyTestSdk())

        return newFixture
    }

    override val fixture: CodeInsightTestFixture
        get() = lazyFixture.value
}

internal class PythonModuleFixtureBuilder(fixtureBuilder: TestFixtureBuilder<out IdeaProjectTestFixture>) :
    ModuleFixtureBuilderImpl<ModuleFixture>(PlatformPythonModuleType(), fixtureBuilder),
    ModuleFixtureBuilder<ModuleFixture> {

    override fun instantiateFixture(): ModuleFixture {
        return ModuleFixtureImpl(this)
    }
}

internal class PlatformPythonModuleType : PythonModuleTypeBase<EmptyModuleBuilder>() {
    override fun createModuleBuilder(): EmptyModuleBuilder {
        return object : EmptyModuleBuilder() {
            override fun getModuleType(): ModuleType<EmptyModuleBuilder> {
                return instance
            }
        }
    }

    companion object {
        val instance: PlatformPythonModuleType
            get() = ModuleTypeManager.getInstance().findByID(PYTHON_MODULE) as PlatformPythonModuleType
    }
}

internal class PyTestSdk() : ProjectJdkImpl("Fake CPython", PythonSdkType.getInstance()) {
    init {
        sdkAdditionalData = PythonSdkAdditionalData(object : CPythonSdkFlavor() {
            @NotNull
            override fun getName(): String {
                return "Fake CPython"
            }
        })
    }

    override fun getVersionString(): String? {
        return "Fake CPython 3.7.0"
    }
}