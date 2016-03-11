package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage

class EMail(node: ASTNode) : PropertyValue(node), Checkable {

    override fun check(): List<ErrorMessage> {
        if (!node.text.matches("^.+@.+\\..+$".toRegex())) return listOf(ErrorMessage(this, "invalid value", "error"))
        return listOf()
    }
}
