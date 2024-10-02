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
import dev.rakiiii.healthycoroutines.api.ThrowableFilter
import dev.rakiiii.healthycoroutines.api.defaults.DefaultThrowableFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine exception handler restores the coroutine context of exception and start new coroutine with it in [CoroutineScopeHolder] scope
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @param internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] handler for errors thrown in new coroutine
 * @param internalHandler Coroutine code for started exception handling coroutine
 */
fun CoroutineScopeHolder.restoreContextOnError(
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: ContextRestorableCoroutineExceptionHandler.ErrorHandler,
): BaseCoroutineExceptionHandler =
    coroutineScope.restoreContextOnError(internalCoroutineExceptionHandler, internalHandler)

/**
 * Coroutine exception handler restores the coroutine context of exception and start new coroutine with it in [CoroutineScope]
 *
 * Uses [DefaultThrowableFilter] for exception filtering purpose
 *
 * @param internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] handler for errors thrown in new coroutine
 * @param internalHandler Coroutine code for started exception handling coroutine
 */
fun CoroutineScope.restoreContextOnError(
    internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler = logError(),
    internalHandler: ContextRestorableCoroutineExceptionHandler.ErrorHandler,
): BaseCoroutineExceptionHandler = ContextRestorableCoroutineExceptionHandler(
    coroutineScope = this,
    internalHandler = internalHandler,
    throwableFilter = DefaultThrowableFilter,
    internalCoroutineExceptionHandler = internalCoroutineExceptionHandler,
)

/**
 * Coroutine exception handler restores the coroutine context of exception and start new coroutine with it in [coroutineScope]
 *
 * @param coroutineScope [CoroutineScope]
 * @property internalHandler Coroutine code for started exception handling coroutine
 * @property throwableFilter [ThrowableFilter]
 * @property internalCoroutineExceptionHandler [BaseCoroutineExceptionHandler] handler for errors thrown in new coroutine
 */
open class ContextRestorableCoroutineExceptionHandler(
    coroutineScope: CoroutineScope,
    protected val internalHandler: ErrorHandler,
    protected val throwableFilter: ThrowableFilter,
    protected val internalCoroutineExceptionHandler: BaseCoroutineExceptionHandler,
) : BaseCoroutineExceptionHandler() {
    protected val coroutineScope = WeakReference(coroutineScope)

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (throwableFilter(exception)) return
        val coroutineScope = coroutineScope.get() ?: return
        coroutineScope.launch(modifyContext(context)) { internalHandler(exception) }
    }

    protected open fun modifyContext(context: CoroutineContext): CoroutineContext {
        return context.minusKey(Job) + internalCoroutineExceptionHandler
    }

    fun interface ErrorHandler {
        suspend operator fun invoke(exception: Throwable)
    }
}