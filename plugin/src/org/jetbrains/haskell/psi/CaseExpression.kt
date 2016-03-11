package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode


/**
 * Created by atsky on 10/04/14.
 */
class CaseExpression(node : ASTNode) : Expression(node) {
    override fun traverse(visitor: (Expression) -> Unit) {
    }
}