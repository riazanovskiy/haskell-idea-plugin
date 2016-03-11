package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

/**
 * Created by atsky on 4/11/14.
 */
class DataDeclaration(node : ASTNode) : Declaration(node) {

    override fun getDeclarationName(): String? {
        return getNameElement()?.getNameText()
    }

    fun getNameElement(): TypeVariable?  =
        PsiTreeUtil.getChildrenOfTypeAsList(this, TypeVariable::class.java).firstOrNull()

    fun getConstructorDeclarationList() : List<ConstructorDeclaration> =
        PsiTreeUtil.getChildrenOfTypeAsList(this, ConstructorDeclaration::class.java)


}