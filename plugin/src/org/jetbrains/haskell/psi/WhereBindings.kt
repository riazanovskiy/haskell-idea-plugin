package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Created by atsky on 11/21/14.
 */
class WhereBindings(node : ASTNode) : ASTWrapperPsiElement(node) {
    fun getSignatureDeclarationsList() : List<SignatureDeclaration> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, SignatureDeclaration::class.java)
    }

    fun getValueDefinitionList() : List<ValueDefinition> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ValueDefinition::class.java)
    }
}