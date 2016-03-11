package org.jetbrains.cabal.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.cabal.highlight.ErrorMessage

class FullCondition(node: ASTNode) : ASTWrapperPsiElement(node), Checkable {

    override fun check(): List<ErrorMessage> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ConditionPart::class.java).flatMap { it.checkBrackets() }
    }
}
