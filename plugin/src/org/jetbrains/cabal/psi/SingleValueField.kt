package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.cabal.highlight.ErrorMessage

open class SingleValueField(node: ASTNode) : PropertyField(node) {

    open fun checkUniqueness(): ErrorMessage? {
        fun isSame(field: PsiElement) = (field is PropertyField) && (field.hasName(getFieldName()))

        if ((parent!!.children.filter({ isSame(it) })).size > 1) {
            return ErrorMessage(getKeyNode(), "duplicate field", "error")
        }
        return null
    }

    fun getValue(): PropertyValue? {
        return PsiTreeUtil.findChildOfType(this, PropertyValue::class.java)
    }
}
