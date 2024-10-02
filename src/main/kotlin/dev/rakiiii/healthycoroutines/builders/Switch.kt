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

import dev.rakiiii.healthycoroutines.api.CustomCoroutineDispatcher
import dev.rakiiii.healthycoroutines.api.defaults.DefaultDependentCoroutineContextConverter
import dev.rakiiii.healthycoroutines.api.defaults.DefaultMainDispatcherModifier
import dev.rakiiii.healthycoroutines.api.dependentcontext.DependentCoroutineContext
import dev.rakiiii.healthycoroutines.api.dependentcontext.completionExceptionHandler
import dev.rakiiii.healthycoroutines.handlers.logAndRethrowError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coroutine builder to switch dispatcher to IO inside coroutine
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param block coroutine code
 */
suspend inline fun <R> switchToIo(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): R = withContext(Dispatchers.IO + DefaultDependentCoroutineContextConverter(context), block = block)

/**
 * Coroutine builder to switch dispatcher to Default inside coroutine
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param block coroutine code
 */
suspend inline fun <R> switchToComputation(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): R = withContext(Dispatchers.Default + DefaultDependentCoroutineContextConverter(context), block = block)

/**
 * Coroutine builder to switch dispatcher to Main inside coroutine
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param block coroutine code
 */
suspend inline fun <R> switchToUi(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    noinline block: suspend CoroutineScope.() -> R,
): R = withContext(DefaultMainDispatcherModifier(Dispatchers.Main) + DefaultDependentCoroutineContextConverter(context), block = block)

/**
 * Coroutine builder to switch dispatcher to CustomDispatcher inside coroutine
 *
 * @param context [DependentCoroutineContext] by default contains [logAndRethrowError]
 * @param block coroutine code
 */
suspend inline fun <R> switchToCustomDispatcher(
    context: DependentCoroutineContext = completionExceptionHandler(logAndRethrowError()),
    dispatcher: CustomCoroutineDispatcher,
    noinline block: suspend CoroutineScope.() -> R,
): R = withContext(dispatcher + DefaultDependentCoroutineContextConverter(context), block = block)
