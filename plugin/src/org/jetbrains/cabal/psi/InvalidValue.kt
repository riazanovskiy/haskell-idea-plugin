package org.jetbrains.cabal.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import org.jetbrains.cabal.highlight.ErrorMessage

open class InvalidValue(node: ASTNode) : ASTWrapperPsiElement(node), Checkable {

    override fun check(): List<ErrorMessage> {
        if (text!! == "") {
            return listOf(ErrorMessage(this, "invalid empty value", "error", isAfterNodeError = true))
        }
        return listOf(ErrorMessage(this, "invalid value", "error"))
    }
}
