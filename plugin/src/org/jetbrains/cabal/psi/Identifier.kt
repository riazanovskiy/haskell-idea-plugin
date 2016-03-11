package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage

class Identifier(node: ASTNode) : PropertyValue(node), Checkable {

    override fun check(): List<ErrorMessage> {
        if (!node.text.matches("^[a-zA-Z](\\w|[.-])*$".toRegex())) {
            return listOf(ErrorMessage(this, "invalid identifier", "error"))
        }
        return listOf()
    }
}