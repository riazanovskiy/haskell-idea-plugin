package org.jetbrains.haskell.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.jetbrains.haskell.psi.Expression
import org.jetbrains.haskell.psi.QVar
import org.jetbrains.haskell.scope.ExpressionScope

/**
 * Created by atsky on 4/25/14.
 */
class ValueReference(val referenceExpression: QVar) : PsiReferenceBase<QVar>(
        referenceExpression,
        TextRange(0, referenceExpression.textRange!!.length)) {

    override fun resolve(): PsiElement? {
        val parent = referenceExpression.parent
        if (parent !is Expression) {
            return null;
        }
        return ExpressionScope(parent).getVisibleVariables().firstOrNull {
            it.text == value
        }
    }


    override fun getVariants(): Array<Any> = arrayOf()

}