package org.jetbrains.haskell.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * Created by atsky on 4/25/14.
 */
abstract class Expression(node : ASTNode) : ASTWrapperPsiElement(node) {
    abstract fun traverse(visitor : (Expression) -> Unit)
}
