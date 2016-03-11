package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

open class PropertyField(node: ASTNode) : Field(node) {

    fun getKeyNode(): PsiElement = firstChild!!

    fun getPropertyName(): String = getKeyNode().text!!
}
