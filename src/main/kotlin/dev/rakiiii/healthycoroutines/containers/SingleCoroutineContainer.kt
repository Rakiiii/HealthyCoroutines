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
 * Coroutine container that give a guarantee that is only single active coroutine in the container
 * exists in any moment of time
 *
 * Current coroutine being canceled when new coroutine is started
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val searchSingleJobContainer = singleCoroutineContainer()
 *
 *     fun onQueryChanged(query: String?) {
 *          searchSingleJobContainer.onIo {
 *              // ... update by fetched data
 *          }
 *     }
 * }
 * ```
 */
fun <T> T.singleCoroutineContainer(): BaseCoroutineContainer where T : CoroutineScopeHolder {
    return SingleCoroutineContainer(coroutineScope)
}

/**
 * Coroutine container that give a guarantee that is only single active coroutine in the container
 * exists in any moment of time
 *
 * Current coroutine being canceled when new coroutine is started
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val searchSingleJobContainer = singleCoroutineContainer()
 *
 *     fun onQueryChanged(query: String?) {
 *          searchSingleJobContainer.onIo {
 *              // ... update by fetched data
 *          }
 *     }
 * }
 * ```
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
fun singleCoroutineContainer(coroutineScope: CoroutineScope): BaseCoroutineContainer {
    return SingleCoroutineContainer(coroutineScope)
}

/**
 * Coroutine container that give a guarantee that is only single active coroutine in the container
 * exists in any moment of time
 *
 * Current coroutine being canceled when new coroutine is started
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
private class SingleCoroutineContainer(
    coroutineScope: CoroutineScope,
) : TrivialCoroutineContainer(coroutineScope) {
    private var job: Job? = null
    override val isActive: Boolean
        get() = job?.isActive == true

    override fun set(job: Job) {
        cancel()
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
