package org.jetbrains.haskell.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode


class ValueDefinition(node : ASTNode) : ASTWrapperPsiElement(node) {
    fun getQNameExpression(): QNameExpression? =
            findChildByClass(QNameExpression::class.java)

    fun getExpression(): Expression? =
            findChildByClass(Expression::class.java)
}