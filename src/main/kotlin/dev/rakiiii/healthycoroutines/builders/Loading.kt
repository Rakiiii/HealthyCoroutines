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

package dev.rakiiii.healthycoroutines.builders

import dev.rakiiii.healthycoroutines.api.CoroutineScopeHolder
import dev.rakiiii.healthycoroutines.api.Loadable
import dev.rakiiii.healthycoroutines.api.dependentcontext.DependentCoroutineContext
import dev.rakiiii.healthycoroutines.api.dependentcontext.completionExceptionHandler
import dev.rakiiii.healthycoroutines.handlers.logAndRethrowError
import dev.rakiiii.healthycoroutines.handlers.logError
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine builder for getting IO operation result with loading showing on [Loadable]
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param operation coroutine code
 */
suspend inline fun <T, R : Any> T.getIoWithLoading(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline operation: suspend () -> R,
): R where T : CoroutineScopeHolder,
           T : Loadable {
    return getIoWithLoading(context, this::setLoading, operation)
}

/**
 * Coroutine builder for getting IO operation result with loading showing
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param updateLoading Loading flag callback, called on IO Dispatcher
 * @param operation coroutine code
 */
suspend inline fun <R : Any> CoroutineScopeHolder.getIoWithLoading(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline updateLoading: suspend (Boolean) -> Unit,
    crossinline operation: suspend () -> R,
): R {
    return startIo(context) { withLoading(updateLoading, operation) }.await()
}

/**
 * Coroutine builder for getting computation operation result with loading showing on [Loadable]
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param operation coroutine code
 */
suspend inline fun <T, R : Any> T.getComputationWithLoading(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline operation: suspend () -> R,
): R where T : CoroutineScopeHolder,
           T : Loadable {
    return getComputationWithLoading(context, this::setLoading, operation)
}

/**
 * Coroutine builder for getting computation operation result with loading showing
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param updateLoading Loading flag callback, called on Default Dispatcher
 * @param operation coroutine code
 */
suspend inline fun <R : Any> CoroutineScopeHolder.getComputationWithLoading(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    crossinline updateLoading: suspend (Boolean) -> Unit,
    crossinline operation: suspend () -> R,
): R {
    return startComputation (context) { withLoading(updateLoading, operation) }.await()
}

/**
 * Coroutine builder for do IO operation with loading showing on [Loadable]
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param operation coroutine code
 */
inline fun <T> T.doIoWithLoading(
    context: CoroutineContext = logError(),
    crossinline operation: suspend () -> Unit,
) where T : CoroutineScopeHolder,
        T : Loadable {
    return doIoWithLoading(context, this::setLoading, operation)
}

/**
 * Coroutine builder for do IO operation with loading showing
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param updateLoading Loading flag callback, called on IO Dispatcher
 * @param operation coroutine code
 */
inline fun CoroutineScopeHolder.doIoWithLoading(
    context: CoroutineContext = logError(),
    crossinline updateLoading: suspend (Boolean) -> Unit,
    crossinline operation: suspend () -> Unit,
) {
    onIo(context) { withLoading(updateLoading, operation) }
}

/**
 * Coroutine builder for do computation operation with loading showing on [Loadable]
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param operation coroutine code
 */
inline fun <T> T.doComputationWithLoading(
    context: CoroutineContext = logError(),
    crossinline operation: suspend () -> Unit,
) where T : CoroutineScopeHolder,
        T : Loadable {
    return doComputationWithLoading(context, this::setLoading, operation)
}

/**
 * Coroutine builder for do computation operation with loading showing
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param updateLoading Loading flag callback, called on Default Dispatcher
 * @param operation coroutine code
 */
inline fun CoroutineScopeHolder.doComputationWithLoading(
    context: CoroutineContext = logError(),
    crossinline updateLoading: suspend (Boolean) -> Unit,
    crossinline operation: suspend () -> Unit,
) {
    onComputation(context) { withLoading(updateLoading, operation) }
}

/**
 * Base function for suspend operation with loading on [Loadable]
 */
suspend inline fun <T : Loadable, R> T.withLoading(crossinline operation: suspend () -> R): R {
    return withLoading(updateLoading = ::setLoading, operation)
}

/**
 * Base function for suspend operation with loading
 *
 * @param updateLoading Loading flag callback
 * @param operation main operation
 */
suspend inline fun <R> withLoading(
    crossinline updateLoading: suspend (isLoading: Boolean) -> Unit,
    crossinline operation: suspend () -> R,
): R {
    updateLoading(true)
    return try {
        operation()
    } finally {
        updateLoading(false)
    }
}
