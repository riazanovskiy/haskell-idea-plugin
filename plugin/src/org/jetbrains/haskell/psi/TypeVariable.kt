package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import org.jetbrains.haskell.psi.reference.TypeReference
import org.jetbrains.haskell.psi.util.HaskellElementFactory

/**
 * Created by atsky on 4/11/14.
 */
class TypeVariable(node: ASTNode) : HaskellType(node), PsiNamedElement {

    override fun getName(): String? {
        return text
    }

    override fun getLeftTypeVariable() : TypeVariable? {
        return this;
    }

    override fun setName(name: String): PsiElement? {
        val qcon = HaskellElementFactory.createExpressionFromText(project, name)
        firstChild.replace(qcon)
        return qcon
    }

    fun getNameText() : String? {
        return text
    }

    override fun getReference(): PsiReference? =
        if (!isConstructor()) {
            TypeReference(this)
        } else {
            null
        }


    fun isClass() : Boolean {
        var current : PsiElement? = this
        while (true) {
            val parent = current!!.parent
            if (parent is Context) {
                return true
            }
            if (parent is ClassDeclaration) {
                return true
            }
            if (parent !is HaskellType) {
                return false
            }
            current = parent
        }
    }

    fun isConstructor() : Boolean {
        var current : PsiElement? = this
        while (true) {
            val parent = current!!.parent
            if (parent is ConstructorDeclaration) {
                return true
            }
            if (parent !is ApplicationType || parent.getChildrenTypes()[0] != current) {
                return false
            }
            current = parent
        }
    }
}