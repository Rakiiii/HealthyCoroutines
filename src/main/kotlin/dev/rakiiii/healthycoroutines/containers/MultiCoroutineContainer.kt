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

import java.util.Collections
import dev.rakiiii.healthycoroutines.api.BaseCoroutineContainer
import dev.rakiiii.healthycoroutines.api.CoroutineScopeHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Coroutine container which aggregates multiple started coroutines at the same time and
 * cancellable at same time, main goal of this container is to work with multiple coroutines as with single coroutine
 *
 * When coroutine started it simple added to coroutine list
 *
 * Coroutine container active while at least 1 coroutine inside it is active
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val analyticsLoadContainer = multiCoroutineContainer()
 *
 *     fun onAnalyticsLoadRequested(query: String?) {
 *          analyticsLoadContainer.onIo {
 *              // ... update by fetched data
 *          }
 *     }
 *
 *     fun onSomeUserActionRequested() {
 *          analyticsLoadContainer.cancel()
 *          // ...
 *     }
 * }
 * ```
 */
fun <T> T.multiCoroutineContainer(): BaseCoroutineContainer where T : CoroutineScopeHolder {
    return MultiCoroutineContainer(coroutineScope)
}

/**
 * Coroutine container which aggregates multiple started coroutines at the same time and
 * cancellable at same time
 *
 * When coroutine started it simple added to coroutine list
 *
 * Coroutine container active while at least 1 coroutine inside it is active
 *
 * Example,
 *
 * ```
 * class SomeViewModel : CoroutineScopeHolder {
 *     private val analyticsLoadContainer = multiCoroutineContainer()
 *
 *     fun onAnalyticsLoadRequested(query: String?) {
 *          analyticsLoadContainer.onIo {
 *              // ... update by fetched data
 *          }
 *     }
 *
 *     fun onSomeUserActionRequested() {
 *          analyticsLoadContainer.cancel()
 *          // ...
 *     }
 * }
 * ```
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
fun multiCoroutineContainer(coroutineScope: CoroutineScope): BaseCoroutineContainer {
    return MultiCoroutineContainer(coroutineScope)
}

/**
 * Coroutine container which aggregates multiple started coroutines at the same time and
 * cancellable at same time
 *
 * When coroutine started it simple added to coroutine list
 *
 * Coroutine container active while at least 1 coroutine inside it is active
 *
 * @param coroutineScope [CoroutineScope] for all coroutines started in the container
 */
open class MultiCoroutineContainer(
    coroutineScope: CoroutineScope,
) : TrivialCoroutineContainer(coroutineScope) {
    private val jobs: MutableList<Job> = Collections.synchronizedList(mutableListOf())
    override val isActive: Boolean
        get() = jobs.any { it.isActive }

    override fun cancel() {
        jobs.forEach { job -> if (job.isActive) job.cancel() }
        jobs.clear()
    }

    override fun set(job: Job) {
        jobs.add(job)
    }
}
