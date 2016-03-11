package org.jetbrains.cabal.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

open class PropertyValue(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getText(): String = node.text

}