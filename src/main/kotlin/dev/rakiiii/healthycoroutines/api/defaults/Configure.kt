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

package dev.rakiiii.healthycoroutines.api.defaults

import dev.rakiiii.healthycoroutines.api.CatchingLogger
import dev.rakiiii.healthycoroutines.api.CoroutineLogger
import dev.rakiiii.healthycoroutines.api.DispatcherModifier
import dev.rakiiii.healthycoroutines.api.ThrowableFilter
import dev.rakiiii.healthycoroutines.api.defaults.DefaultCancellationExceptionHandler.Handler

/**
 * Framework configuration function
 *
 * @param coroutineCancellationHandler [Handler] Default handler for addition work on all coroutine cancellations, if you need to log something - this is the best place
 * @param logger [CoroutineLogger] Default logger for all logging coroutine exception handlers
 * @param catchingLogger [CatchingLogger] Default logger for all catching helpers
 * @param mainDispatcherModifier [DispatcherModifier] Dispatcher modifier to change something in DispatcherMain, if you need to make it immediate - this is the best place
 * @param throwableFilter [ThrowableFilter] Default global default filter for all throwable in ExceptionHandlers, any throwable that matches this filter will be ignored
 */
fun configureHealthyCoroutines(
    coroutineCancellationHandler: Handler,
    logger: CoroutineLogger,
    catchingLogger: CatchingLogger,
    mainDispatcherModifier: DispatcherModifier = DispatcherModifier.ID,
    throwableFilter: ThrowableFilter = ThrowableFilter.NONE,
) {
    DefaultCancellationExceptionHandler.configure(coroutineCancellationHandler)
    DefaultCoroutineLogger.configure(logger)
    DefaultCatchingLogger.configure(catchingLogger)
    DefaultMainDispatcherModifier.configure(mainDispatcherModifier)
    DefaultThrowableFilter.configure(throwableFilter)
}