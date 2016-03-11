package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode

/**
 * @author Evgeny.Kurbatsky
 * @since 10.09.15.
 */
class ForeignDeclaration(node : ASTNode) : Declaration(node) {

    override fun getDeclarationName(): String? {
        return getQVar()?.text
    }

    fun getQVar(): QVar? =
            findChildByClass(QVar::class.java)
}