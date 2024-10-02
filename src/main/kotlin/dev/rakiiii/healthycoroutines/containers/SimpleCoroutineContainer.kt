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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Coroutine container that gives you a chance to cancel coroutine by your self, but do not cancel previous coroutine
 * when you start new coroutine in the container
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val searchSingleJobContainer = simpleCoroutineContainer()
 *
 *     fun onQueryChanged(query: String?) {
 *          searchSingleJobContainer.cancel()
 *          searchSingleJobContainer.onIo {
 *              // ... update by fetched data
 *          }
 *     }
 * }
 * ```
 */
fun <T> T.simpleCoroutineContainer(): BaseCoroutineContainer where T : CoroutineScopeHolder {
    return SimpleCoroutineContainer(coroutineScope)
}

/**
 * Coroutine container that gives you a chance to cancel coroutine by your self, but do not cancel previous coroutine
 * when you start new coroutine in the container
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val searchSingleJobContainer = simpleCoroutineContainer()
 *
 *     fun onQueryChanged(query: String?) {
 *          searchSingleJobContainer.cancel()
 *          searchSingleJobContainer.onIo {
 *              // ... update by fetched data
 *          }
 *     }
 * }
 * ```
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
fun simpleCoroutineContainer(coroutineScope: CoroutineScope): BaseCoroutineContainer {
    return SimpleCoroutineContainer(coroutineScope)
}

/**
 * Coroutine container that gives you a chance to cancel coroutine by your self, but do not cancel previous coroutine
 * when you start new coroutine in the container
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
open class SimpleCoroutineContainer(
    coroutineScope: CoroutineScope,
) : TrivialCoroutineContainer(coroutineScope) {
    private var job: Job? = null
    override val isActive: Boolean
        get() = job?.isActive == true

    override fun set(job: Job) {
        this.job = job
    }

    override fun cancel() {
        val job = this.job
        if (job != null && job.isActive) {
            job.cancel()
        }
        this.job = null
    }
}
