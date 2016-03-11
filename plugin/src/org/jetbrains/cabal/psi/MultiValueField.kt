package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

open class MultiValueField(node: ASTNode) : PropertyField(node) {

    fun <T : PsiElement> getValues(valueType: Class<T>): List<T> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, valueType)
    }
}
