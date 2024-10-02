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

import dev.rakiiii.healthycoroutines.api.BaseCoroutineContainer
import dev.rakiiii.healthycoroutines.api.CoroutineScopeHolder
import dev.rakiiii.healthycoroutines.api.defaults.DefaultCancellationExceptionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Coroutine container api extension to support restarts
 */
abstract class RestartableCoroutineContainer : BaseCoroutineContainer() {
    /** Restarts last started coroutine in the container */
    abstract fun restart()
}

/**
 * Coroutine container that can restart canceled and working coroutine and give a guarantee that is only single
 * active coroutine in the container exists in any moment of time
 *
 * Current coroutine being canceled when new coroutine is started or when restart is called
 *
 * Technically restart mechanism is creating a coroutine with same code and similar context under the hood and starts it
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val fetchPageCoroutineContainer = restartableCoroutineContainer()
 *
 *     fun onNewPageFetchRequested(page: Int) {
 *          fetchPageCoroutineContainer.onIo(onAnyError(::showErrorScreen)) {
 *              // ... update by fetched data
 *          }
 *     }
 *
 *     fun onRefreshFromErrorScreenRequested() {
 *          fetchPageCoroutineContainer.restart()
 *          // ...
 *     }
 * }
 * ```
 */
fun <T> T.restartableCoroutineContainer(): RestartableCoroutineContainer where T : CoroutineScopeHolder {
    return RestartableCoroutineContainerImpl(coroutineScope)
}

/**
 * Coroutine container that can restart canceled and working coroutine and give a guarantee that is only single
 * active coroutine in the container exists in any moment of time
 *
 * Current coroutine being canceled when new coroutine is started or when restart is called
 *
 * Technically restart mechanism is creating a coroutine with same code and similar context under the hood and starts it
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val fetchPageCoroutineContainer = restartableCoroutineContainer()
 *
 *     fun onNewPageFetchRequested(page: Int) {
 *          fetchPageCoroutineContainer.onIo(onAnyError(::showErrorScreen)) {
 *              // ... update by fetched data
 *          }
 *     }
 *
 *     fun onRefreshFromErrorScreenRequested() {
 *          fetchPageCoroutineContainer.restart()
 *          // ...
 *     }
 * }
 * ```
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
fun restartableCoroutineContainer(coroutineScope: CoroutineScope): RestartableCoroutineContainer {
    return RestartableCoroutineContainerImpl(coroutineScope)
}

/**
 * Coroutine container that can restart canceled and working coroutine and give a guarantee that is only single
 * active coroutine in the container exists in any moment of time
 *
 * Current coroutine being canceled when new coroutine is started or when restart is called
 *
 * Technically restart mechanism is creating a coroutine with same code and similar context under the hood and starts it
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
open class RestartableCoroutineContainerImpl(
    private val scope: CoroutineScope,
) : RestartableCoroutineContainer() {
    private var coroutineDescription: CoroutineDesc? = null
    private var job: Job? = null

    override val isActive: Boolean
        get() = job?.isActive == true

    override fun cancel() {
        val job = this.job
        if (job != null && job.isActive) {
            job.cancel()
        }
        this.job = null
    }

    override fun restart() {
        cancel()
        val desc = coroutineDescription
        if (desc != null) startCoroutineFromDesc(desc)
    }

    override fun startCoroutineInternal(
        coroutineDispatcher: CoroutineDispatcher,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        cancel()
        val desc = CoroutineDesc(block = block, context = context, coroutineDispatcher = coroutineDispatcher)
        coroutineDescription = desc
        startCoroutineFromDesc(desc)
    }

    protected fun startCoroutineFromDesc(desc: CoroutineDesc) {
        job = scope.launch(desc.coroutineDispatcher + desc.context) {
            try {
                val block = desc.block
                block()
            } catch (e: CancellationException) {
                DefaultCancellationExceptionHandler(e)
            }
        }
    }

    protected class CoroutineDesc(
        val block: suspend CoroutineScope.() -> Unit,
        val context: CoroutineContext,
        val coroutineDispatcher: CoroutineDispatcher,
    )
}
