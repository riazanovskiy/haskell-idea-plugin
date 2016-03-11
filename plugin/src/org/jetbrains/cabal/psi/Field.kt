package org.jetbrains.cabal.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

open class Field(node: ASTNode) : ASTWrapperPsiElement(node) {

    fun getType(): IElementType = node.elementType

    fun hasName(name: String): Boolean {
        return firstChild!!.text!!.equals(name, ignoreCase = true)
    }

    fun getFieldName(): String {
        return firstChild!!.text!!.toLowerCase()
    }
}
