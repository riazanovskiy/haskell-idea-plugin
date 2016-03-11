package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage

class Token(node: ASTNode) : PropertyValue(node), Checkable {

    override fun check(): List<ErrorMessage> {
        if (!node.text.matches("^.+$".toRegex())) {
            return listOf(ErrorMessage(this, "invalid token", "error"))
        }
        return listOf()
    }
}
