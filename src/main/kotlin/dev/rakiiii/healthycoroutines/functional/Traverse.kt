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

package dev.rakiiii.healthycoroutines.functional

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

/**
 * Simple functional traverse function for Iterable
 *
 * @param computation [Computation]
 */
suspend fun <T : Any, R : Any> Iterable<T>.traverse(computation: Computation<T, R>) = map { computation(it) }

/**
 * Simple functional traverse function for Array
 *
 * @param computation [Computation]
 */
suspend fun <T : Any, R : Any> Array<T>.traverse(computation: Computation<T, R>) = map { computation(it) }

/**
 * Simple functional traverse function for Map
 *
 * @param computation [Computation]
 */
suspend fun <K : Any, V : Any, R : Any> Map<K, V>.traverse(computation: Computation<Map.Entry<K, V>, R>) =
    map { computation(it) }

/**
 * Async computation, only starts computation
 *
 * @param scope [CoroutineScope] for computation start
 * @param context [CoroutineContext] for computation start
 * @param morphism [Computation]
 */
fun <T : Any, R : Any> async(context: CoroutineContext, scope: CoroutineScope, morphism: Computation<T, R>) =
    Computation<T, Deferred<R>> { element ->
        scope.async(context) { morphism(element) }
    }

/**
 * Computation for async result waiting
 */
fun <T : Any> await() = Computation<Deferred<T>, T> {
    it.await()
}

/**
 * Suspend computation api
 */
fun interface Computation<T : Any, R : Any> {
    suspend operator fun invoke(value: T): R
}
