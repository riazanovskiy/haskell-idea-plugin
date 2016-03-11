package org.jetbrains.cabal.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import org.jetbrains.cabal.parser.LANGUAGE_VALS

class Language(node: ASTNode) : ASTWrapperPsiElement(node), RangedValue {
    override fun getAvailableValues(): List<String> {
        return LANGUAGE_VALS
    }
}
