package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage

class InvalidField(node: ASTNode) : Field(node), Checkable {

    override fun check(): List<ErrorMessage> {
        return listOf(ErrorMessage(this, "invalid field", "error"))
    }
}
