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

import dev.rakiiii.healthycoroutines.api.DispatcherModifier
import kotlinx.coroutines.CoroutineDispatcher

/**
 *  Configurator for default dispatcher modifier to change something in DispatcherMain, if you need to make it immediate - this is the best place
 */
object DefaultMainDispatcherModifier : DispatcherModifier {
    private var _modifier: DispatcherModifier = DispatcherModifier.ID

    override fun invoke(dispatcher: CoroutineDispatcher) = _modifier(dispatcher)

    fun configure(modifier: DispatcherModifier) {
        _modifier = modifier
    }
}