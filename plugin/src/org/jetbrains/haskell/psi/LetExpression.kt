package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode

/**
 * Created by atsky on 21/04/14.
 */

class LetExpression(node : ASTNode) : Expression(node) {
    override fun traverse(visitor: (Expression) -> Unit) {
        visitor(this)
    }
}