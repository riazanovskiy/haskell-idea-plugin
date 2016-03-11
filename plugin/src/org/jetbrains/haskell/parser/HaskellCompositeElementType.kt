package org.jetbrains.haskell.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.jetbrains.haskell.HaskellLanguage


open class HaskellCompositeElementType(
        debugName: String,
        val constructor : ((ASTNode)->PsiElement)? = null) :
                                IElementType(debugName, HaskellLanguage.INSTANCE) {

    private val myDebugName: String = debugName


    open fun getDebugName(): String {
        return myDebugName
    }
}
