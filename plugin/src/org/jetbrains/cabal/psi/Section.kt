package org.jetbrains.cabal.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.jetbrains.cabal.highlight.ErrorMessage

open class Section(node: ASTNode): Field(node), FieldContainer, Checkable {

    override fun check(): List<ErrorMessage> = listOf()

    fun getSectChildren(): List<PsiElement> = children.filter { it is Field }

    fun getSectTypeNode(): PsiElement = (children.firstOrNull { it is SectionType }) ?: throw IllegalStateException()

    fun getSectType(): String = getSectTypeNode().text!!

    protected open fun getSectName(): String? {
        var node = firstChild
        while ((node != null) && (node !is Name)) {
            node = node.nextSibling
        }
        return (node as? Name)?.text
    }
}