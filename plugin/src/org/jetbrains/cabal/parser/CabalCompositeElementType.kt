package org.jetbrains.cabal.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.jetbrains.cabal.CabalLanguage

class CabalCompositeElementType(val myDebugName: String, val contructor : (ASTNode) -> PsiElement) : IElementType(myDebugName, CabalLanguage.INSTANCE) {

    fun getDebugName(): String {
        return myDebugName
    }

}
