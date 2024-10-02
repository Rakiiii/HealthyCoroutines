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

package dev.rakiiii.healthycoroutines.handlers

import dev.rakiiii.healthycoroutines.api.BaseCoroutineExceptionHandler
import dev.rakiiii.healthycoroutines.api.CoroutineScopeHolder
import dev.rakiiii.healthycoroutines.api.CustomCoroutineDispatcher
import dev.rakiiii.healthycoroutines.api.ThrowableFilter
import dev.rakiiii.healthycoroutines.api.defaults.DefaultMainDispatcherModifier
import dev.rakiiii.healthycoroutines.api.defaults.DefaultThrowableFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine exception handler that handles exception in coroutine started on CustomDispatcher with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScopeHolder.onCustomDispatcherOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    dispatcher: CustomCoroutineDispatcher,
    internalHandler: SuspendableErrorHandler,
) = coroutineScope.onCustomDispatcherOnError(
    throwableFilter,
    internalCoroutineExceptionHandler,
    dispatcher,
    internalHandler,
)

/**
 * Coroutine exception handler that handles exception in coroutine started on CustomDispatcher with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScope.onCustomDispatcherOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    dispatcher: CustomCoroutineDispatcher,
    internalHandler: SuspendableErrorHandler,
): BaseCoroutineExceptionHandler = SimpleSuspendableCoroutineExceptionHandler(
    coroutineScope = this,
    dispatcher = dispatcher,
    internalHandler = internalHandler,
    throwableFilter = throwableFilter,
    internalCoroutineExceptionHandler = internalCoroutineExceptionHandler,
)

/**
 * Coroutine exception handler that handles exception in UI coroutine with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScopeHolder.uiOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: SuspendableErrorHandler,
) = coroutineScope.uiOnError(throwableFilter, internalCoroutineExceptionHandler, internalHandler)

/**
 * Coroutine exception handler that handles exception in UI coroutine with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScope.uiOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: SuspendableErrorHandler,
): BaseCoroutineExceptionHandler = SimpleSuspendableCoroutineExceptionHandler(
    coroutineScope = this,
    dispatcher = DefaultMainDispatcherModifier(Dispatchers.Main),
    internalHandler = internalHandler,
    throwableFilter = throwableFilter,
    internalCoroutineExceptionHandler = internalCoroutineExceptionHandler,
)

/**
 * Coroutine exception handler that handles exception in computation coroutine with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScopeHolder.computationOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: SuspendableErrorHandler,
) = coroutineScope.computationOnError(throwableFilter, internalCoroutineExceptionHandler, internalHandler)

/**
 * Coroutine exception handler that handles exception in computation coroutine with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScope.computationOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: SuspendableErrorHandler,
): BaseCoroutineExceptionHandler = SimpleSuspendableCoroutineExceptionHandler(
    coroutineScope = this,
    dispatcher = Dispatchers.Default,
    internalHandler = internalHandler,
    throwableFilter = throwableFilter,
    internalCoroutineExceptionHandler = internalCoroutineExceptionHandler,
)

/**
 * Coroutine exception handler that handles exception in IO coroutine with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScopeHolder.ioOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: SuspendableErrorHandler,
) = coroutineScope.ioOnError(throwableFilter, internalCoroutineExceptionHandler, internalHandler)

/**
 * Coroutine exception handler that handles exception in IO coroutine with [internalCoroutineExceptionHandler]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter] by default is [DefaultThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] by default is [logError]
 */
fun CoroutineScope.ioOnError(
    throwableFilter: ThrowableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: SuspendableErrorHandler,
): BaseCoroutineExceptionHandler = SimpleSuspendableCoroutineExceptionHandler(
    coroutineScope = this,
    dispatcher = Dispatchers.IO,
    internalHandler = internalHandler,
    throwableFilter = throwableFilter,
    internalCoroutineExceptionHandler = internalCoroutineExceptionHandler,
)

/**
 * Coroutine exception handler that starts new coroutine with [dispatcher] + [internalCoroutineExceptionHandler]
 * in [coroutineScope] on error and calls [internalHandler] if exception does not match [throwableFilter]
 *
 * @param coroutineScope [CoroutineScope]
 * @property dispatcher [Dispatchers]
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] handler for errors thrown in new coroutine
 */
private class SimpleSuspendableCoroutineExceptionHandler(
    coroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val internalHandler: SuspendableErrorHandler,
    private val throwableFilter: ThrowableFilter,
    private val internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler,
) : BaseCoroutineExceptionHandler() {

    private val coroutineScope = WeakReference(coroutineScope)

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (throwableFilter(exception)) return
        val coroutineScope = coroutineScope.get() ?: return
        coroutineScope.launch(createContext()) { internalHandler(exception) }
    }

    private fun createContext(): CoroutineContext {
        return dispatcher + internalCoroutineExceptionHandler
    }
}

/** Error handler api */
fun interface SuspendableErrorHandler {
    suspend operator fun invoke(exception: Throwable)
}