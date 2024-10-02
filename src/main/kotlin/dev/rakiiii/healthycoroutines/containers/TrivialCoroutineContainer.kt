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

package dev.rakiiii.healthycoroutines.containers

import dev.rakiiii.healthycoroutines.api.AsyncableBaseCoroutineContainer
import dev.rakiiii.healthycoroutines.api.SimpleDeferred
import dev.rakiiii.healthycoroutines.api.defaults.DefaultCancellationExceptionHandler
import dev.rakiiii.healthycoroutines.api.defaults.DefaultDependentCoroutineContextConverter
import dev.rakiiii.healthycoroutines.api.dependentcontext.DependentCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Base class for basic coroutine containers
 */
abstract class TrivialCoroutineContainer(
    protected val coroutineScope: CoroutineScope,
) : AsyncableBaseCoroutineContainer() {
    override fun startCoroutineInternal(
        coroutineDispatcher: CoroutineDispatcher,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope.launch(coroutineDispatcher + context) {
            try {
                block()
            } catch (e: CancellationException) {
                DefaultCancellationExceptionHandler(e)
            }
        }.let(::set)
    }

    override fun <T> startCoroutineWithResultInternal(
        coroutineDispatcher: CoroutineDispatcher,
        context: DependentCoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): SimpleDeferred<T> {
        return coroutineScope.async(
            context = coroutineDispatcher + DefaultDependentCoroutineContextConverter(context),
            block = block,
        )
            .also { set(it) }
            .wrap()
    }

    protected abstract fun set(job: Job)
}