/*
 * Copyright 2024-today Evgeniy S. Sudarskiy 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.rakiiii.healthycoroutines

import dev.rakiiii.healthycoroutines.api.detektrules.ForbiddenRunCatchingUse
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
class ForbiddenRunCatchingUseTest(private val env: KotlinCoreEnvironment) {
    @Test
    fun runCatchingInSuspendFunction() {
        val code = """
            | suspend fun suspFunc(someFunc:() -> Int) {
            |       val res = runCatching { someFunc() }.getOrElse { 0 }
            | }
        """.trimIndent()
        val config = TestConfig(Config.ACTIVE_KEY to true)
        val findings = ForbiddenRunCatchingUse(config).compileAndLintWithContext(env, code)
        assert(findings.size == 1)
    }

    @Test
    fun runCatchingInViewModelFunction() {
        val code = """
            |package dev.rakiiii
            |   open class ViewModel
            |
            |   open class BaseViewModel : ViewModel()
            |
            |   class SomeViewModel : BaseViewModel() {
            |       fun func(someFunc:() -> Int) {
            |           val res = runCatching { someFunc() }.getOrElse { 0 }
            |       }
            |   }
        """.trimIndent()
        val config = TestConfig(Config.ACTIVE_KEY to true, "coroutineStartSuperClasses" to listOf("ViewModel"))
        val findings = ForbiddenRunCatchingUse(config).compileAndLintWithContext(env, code)
        assert(findings.size == 1)
    }

    @Test
    fun runCatchingInNonSuspendFunction() {
        val code = """
            | fun func(someFunc:() -> Int) {
            |       val res = runCatching { someFunc() }.getOrElse { 0 }
            | }
        """.trimIndent()
        val config = TestConfig(Config.ACTIVE_KEY to true)
        val findings = ForbiddenRunCatchingUse(config).compileAndLintWithContext(env, code)
        assert(findings.size == 0)
    }

    @Test
    fun runCatchingNotForbiddenClassFunction() {
        val code = """
            |package dev.rakiiii
            |   open class Repository
            |
            |   open class BaseRepository : Repository()
            |
            |   class SomeRepository : BaseRepository() {
            |       fun func(someFunc:() -> Int) {
            |           val res = runCatching { someFunc() }.getOrElse { 0 }
            |       }
            |   }
        """.trimIndent()
        val config = TestConfig(Config.ACTIVE_KEY to true, "coroutineStartSuperClasses" to listOf("ViewModel"))
        val findings = ForbiddenRunCatchingUse(config).compileAndLintWithContext(env, code)
        assert(findings.size == 0)
    }
}