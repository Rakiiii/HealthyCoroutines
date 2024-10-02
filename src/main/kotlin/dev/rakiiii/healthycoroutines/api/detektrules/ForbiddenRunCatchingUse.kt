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

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.parentsOfTypeUntil
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SyntheticPropertyDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isDotSelector
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.descriptorUtil.overriddenTreeUniqueAsSequence

/**
 * Detekt rule to make use of runCatching construction forbidden in coroutine related code
 */
@RequiresTypeResolution
class ForbiddenRunCatchingUse(config: Config) : Rule(config) {
    @Configuration("Set of super classes that makes runCatching calls in their child classes forbidden")
    private val coroutineStartSuperClasses: Set<ForbiddenSuperClass> by config(
        valuesWithReason("androidx.lifecycle.ViewModel" to "You cannot use default try catch construction in ViewModel, use helpers instead")
    ) { list ->
        list.map { ForbiddenSuperClass(it.value, it.reason.orEmpty()) }.toSet()
    }

    override val issue = Issue(
        "ForbiddenRunCatchingUse",
        Severity.Defect,
        "Do not use runCatching block in suspend functions, use returnOr* instead.",
        Debt.FIVE_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (isRunCatching(expression)) {
            checkFunction(expression)
            checkClass(expression)
        }
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (isRunCatching(expression.operationReference)) {
            checkFunction(expression)
            checkClass(expression)
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.getCalleeExpressionIfAny()?.isDotSelector() == true && isRunCatching(expression)) {
            checkFunction(expression)
            checkClass(expression)
        }
    }

    override fun visitPrefixExpression(expression: KtPrefixExpression) {
        super.visitPrefixExpression(expression)
        if (isRunCatching(expression.operationReference)) {
            checkFunction(expression)
            checkClass(expression)
        }
    }

    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        if (isRunCatching(expression.operationReference)) {
            checkFunction(expression)
            checkClass(expression)
        }
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        if (isRunCatching(expression.callableReference)) {
            checkFunction(expression)
            checkClass(expression)
        }
    }

    private fun isRunCatching(expression: KtExpression): Boolean {
        val descriptors: Set<CallableDescriptor> =
            expression.getResolvedCall(bindingContext)?.resultingDescriptor?.let { callableDescriptor ->
                val foundDescriptors = if (callableDescriptor is PropertyDescriptor) {
                    setOfNotNull(
                        callableDescriptor.unwrappedGetMethod,
                        callableDescriptor.unwrappedSetMethod,
                    )
                } else {
                    setOf(callableDescriptor)
                }
                foundDescriptors.flatMapTo(mutableSetOf()) {
                    it.overriddenTreeUniqueAsSequence(true).toSet()
                }
            } ?: return false

        return descriptors.find { it.fqNameSafe.asString() == "kotlin.runCatching" } != null
    }

    private fun checkFunction(expression: KtElement) {
        val function = expression.parentsOfTypeUntil<KtNamedFunction, KtClass>().firstOrNull()
        if (function?.modifierList?.getModifier(KtTokens.SUSPEND_KEYWORD) != null) {
            report(CodeSmell(issue, Entity.from(expression), "Suspend function contains try catch"))
        }
    }

    private fun checkClass(expression: KtElement) {
        val ktClass = expression.parentsOfTypeUntil<KtClass, KtFile>().firstOrNull() ?: return
        val superClasses = ktClass.allSuperClassesFqNames()
        val forbiddenEntries = coroutineStartSuperClasses.filter { (text, _) -> superClasses.contains(text) }
        if (forbiddenEntries.isNotEmpty()) {
            report(CodeSmell(issue, Entity.from(expression), forbiddenEntries.first().reason))
        }
    }

    private fun KtClass.allSuperClassesFqNames() =
        bindingContext[BindingContext.CLASS, this]
            ?.getAllSuperClassifiers()
            ?.map { it.fqNameSafe.asString() }
            ?.toList()
            .orEmpty()

    private val PropertyDescriptor.unwrappedGetMethod: FunctionDescriptor?
        get() = if (this is SyntheticPropertyDescriptor) this.getMethod else getter

    private val PropertyDescriptor.unwrappedSetMethod: FunctionDescriptor?
        get() = if (this is SyntheticPropertyDescriptor) this.setMethod else setter

    private data class ForbiddenSuperClass(val name: String, val reason: String)
}