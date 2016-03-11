package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage

class VersionValue(node: ASTNode) : PropertyValue(node), Checkable {

    override fun check(): List<ErrorMessage> {
        if (!this.text.matches("([0-9]+(\\-[0-9a-zA_Z]+)*\\.)*([0-9]+(\\-[0-9a-zA_Z]+)*)".toRegex())) return listOf(ErrorMessage(this, "invalid version", "error"))
        return listOf()
    }
}
