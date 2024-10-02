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

package dev.rakiiii.healthycoroutines.api.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.valuesWithReason
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.parentsOfTypeUntil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers

/**
 * Detekt rule to make 'try catch' calls in suspend functions and methods any wear and in any methods in specialized classes
 */
@RequiresTypeResolution
class ForbiddenTryCatchUse(config: Config) : Rule(config) {
    @Configuration("Set of super classes that makes try catch calls in their child classes forbidden")
    private val coroutineStartSuperClasses: Set<ForbiddenSuperClass> by config(
        valuesWithReason("androidx.lifecycle.ViewModel" to "You cannot use default try catch construction in ViewModel, use helpers instead")
    ) { list ->
        list.map { ForbiddenSuperClass(it.value, it.reason.orEmpty()) }.toSet()
    }

    override val issue = Issue(
        "ForbiddenTryCatchUse",
        Severity.Defect,
        "Do not use try catch block in suspend functions, use returnOr* instead.",
        Debt.FIVE_MINS,
    )

    override fun visitTryExpression(expression: KtTryExpression) {
        val function = expression.parentsOfTypeUntil<KtNamedFunction, KtClass>().firstOrNull()
        if (function?.modifierList?.getModifier(KtTokens.SUSPEND_KEYWORD) != null) {
            report(CodeSmell(issue, Entity.from(expression), "Suspend function contains try catch"))
        }
        val ktClass = expression.parentsOfTypeUntil<KtClass, KtFile>().firstOrNull()
            ?: return super.visitTryExpression(expression)
        val superClasses = ktClass.allSuperClassesFqNames()
        val forbiddenEntries = coroutineStartSuperClasses.filter { (text, _) -> superClasses.contains(text) }
        if (forbiddenEntries.isNotEmpty()) {
            report(CodeSmell(issue, Entity.from(expression), forbiddenEntries.first().reason))
        }
        super.visitTryExpression(expression)
    }

    private fun KtClass.allSuperClassesFqNames() =
        bindingContext[BindingContext.CLASS, this]
            ?.getAllSuperClassifiers()
            ?.map { it.fqNameSafe.asString() }
            ?.toList()
            .orEmpty()

    private data class ForbiddenSuperClass(val name: String, val reason: String)
}