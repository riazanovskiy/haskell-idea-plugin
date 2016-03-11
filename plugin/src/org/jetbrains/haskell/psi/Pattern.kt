package org.jetbrains.haskell.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * Created by atsky on 28/11/14.
 */
class Pattern(node : ASTNode) : ASTWrapperPsiElement(node) {
    fun getExpression(): Expression? =
            findChildByClass(Expression::class.java)
}