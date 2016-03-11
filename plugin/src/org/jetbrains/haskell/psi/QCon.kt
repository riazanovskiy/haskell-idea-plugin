package org.jetbrains.haskell.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.jetbrains.haskell.psi.reference.ConstructorReference

/**
 * Created by atsky on 10/04/14.
 */
class QCon(node : ASTNode) : ASTWrapperPsiElement(node) {

    override fun getReference(): PsiReference? {
        return ConstructorReference(this)
    }
}