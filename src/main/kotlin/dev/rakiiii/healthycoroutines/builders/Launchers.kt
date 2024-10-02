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
import dev.rakiiii.healthycoroutines.api.CustomCoroutineDispatcher
import dev.rakiiii.healthycoroutines.api.defaults.DefaultCancellationExceptionHandler
import dev.rakiiii.healthycoroutines.handlers.logError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Standalone IO coroutine builder
 *
 * @param context [CoroutineContext] by default contains [logError]
 * @param block coroutine code
 */
inline fun CoroutineScopeHolder.onIo(
    context: CoroutineContext = logError(),
    noinline block: suspend CoroutineScope.() -> Unit,
) {
    coroutineScope.onIoInCustomScope(context, block)
}

/**
 * Standalone Default coroutine builder
 *
 * @param context [CoroutineContext] by default contains [logError]
 * @param block coroutine code
 */
inline fun CoroutineScopeHolder.onComputation(
    context: CoroutineContext = logError(),
    noinline block: suspend CoroutineScope.() -> Unit,
) {
    coroutineScope.onComputationInCustomScope(context, block)
}

/**
 * Standalone CustomDispatcher coroutine builder
 *
 * @param context [CoroutineContext] by default contains [logError]
 * @param block coroutine code
 */
inline fun CoroutineScopeHolder.onCustomDispatcher(
    context: CoroutineContext = logError(),
    dispatcher: CustomCoroutineDispatcher,
    noinline block: suspend CoroutineScope.() -> Unit,
) {
    coroutineScope.onCustomDispatcherInCustomScope(context, dispatcher, block)
}

/**
 * Standalone IO coroutine builder
 *
 * @param context [CoroutineContext] by default contains [logError]
 * @param block coroutine code
 */
inline fun CoroutineScope.onIoInCustomScope(
    context: CoroutineContext = logError(),
    noinline block: suspend CoroutineScope.() -> Unit,
) {
    launch(Dispatchers.IO + context) {
        try {
            block()
        } catch (e: CancellationException) {
            DefaultCancellationExceptionHandler(e)
        }
    }
}

/**
 * Standalone Default coroutine builder
 *
 * @param context [CoroutineContext] by default contains [logError]
 * @param block coroutine code
 */
inline fun CoroutineScope.onComputationInCustomScope(
    context: CoroutineContext = logError(),
    noinline block: suspend CoroutineScope.() -> Unit,
) {
    launch(Dispatchers.Default + context) {
        try {
            block()
        } catch (e: CancellationException) {
            DefaultCancellationExceptionHandler(e)
        }
    }
}

/**
 * Standalone CustomDispatcher coroutine builder
 *
 * @param context [CoroutineContext] by default contains [logError]
 * @param block coroutine code
 */
inline fun CoroutineScope.onCustomDispatcherInCustomScope(
    context: CoroutineContext = logError(),
    dispatcher: CustomCoroutineDispatcher,
    noinline block: suspend CoroutineScope.() -> Unit,
) {
    launch(dispatcher + context) {
        try {
            block()
        } catch (e: CancellationException) {
            DefaultCancellationExceptionHandler(e)
        }
    }
}
