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
import dev.rakiiii.healthycoroutines.api.SimpleDeferred
import dev.rakiiii.healthycoroutines.api.defaults.DefaultDependentCoroutineContextConverter
import dev.rakiiii.healthycoroutines.api.dependentcontext.DependentCoroutineContext
import dev.rakiiii.healthycoroutines.api.dependentcontext.completionExceptionHandler
import dev.rakiiii.healthycoroutines.containers.wrap
import dev.rakiiii.healthycoroutines.handlers.logAndRethrowError
import kotlinx.coroutines.*

/**
 * Coroutine builder for async calls on IO dispatcher
 *
 * @param context [DependentCoroutineContext] Context for non-root coroutine, by default contains [logAndRethrowError] for completion error
 * @param block coroutine code
 */
inline fun <R : Any> CoroutineScopeHolder.startIo(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): SimpleDeferred<R> {
    return coroutineScope.async(Dispatchers.IO + DefaultDependentCoroutineContextConverter(context), block = block).wrap()
}

/**
 * Coroutine builder for async calls on Default dispatcher
 *
 * @param context [DependentCoroutineContext] Context for non-root coroutine, by default contains [logAndRethrowError] for completion error
 * @param block coroutine code
 */
inline fun <R> CoroutineScopeHolder.startComputation(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): SimpleDeferred<R> {
    return coroutineScope.async(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context), block = block).wrap()
}

/**
 * Coroutine builder for async calls on IO dispatcher
 *
 * @param context [DependentCoroutineContext] Context for non-root coroutine, by default contains [logAndRethrowError] for completion error
 * @param block coroutine code
 */
inline fun <R : Any> CoroutineScope.startAsyncIoWithCustomScope(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): SimpleDeferred<R> {
    return async(Dispatchers.IO + DefaultDependentCoroutineContextConverter(context), block = block).wrap()
}

/**
 * Coroutine builder for async calls on Default dispatcher
 *
 * @param context [DependentCoroutineContext] Context for non-root coroutine, by default contains [logAndRethrowError] for completion error
 * @param block coroutine code
 */
inline fun <R : Any> CoroutineScope.startComputationInCustomScope(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): SimpleDeferred<R> {
    return async(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context), block = block).wrap()
}

/**
 * Coroutine builder for async calls on custom dispatcher
 *
 * @param context [DependentCoroutineContext] Context for non-root coroutine, by default contains [logAndRethrowError] for completion error
 * @param block coroutine code
 */
inline fun <R> CoroutineScopeHolder.startOnCustomDispatcher(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    dispatcher: CustomCoroutineDispatcher,
    noinline block: suspend CoroutineScope.() -> R,
): SimpleDeferred<R> {
    return coroutineScope.async(dispatcher + DefaultDependentCoroutineContextConverter(context), block = block).wrap()
}

/**
 * Coroutine builder for async calls on custom dispatcher
 *
 * @param context [DependentCoroutineContext] Context for non-root coroutine, by default contains [logAndRethrowError] for completion error
 * @param block coroutine code
 */
inline fun <R : Any> CoroutineScope.startOnCustomDispatcherInCustomScope(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    dispatcher: CustomCoroutineDispatcher,
    noinline block: suspend CoroutineScope.() -> R,
): SimpleDeferred<R> {
    return async(dispatcher + DefaultDependentCoroutineContextConverter(context), block = block).wrap()
}