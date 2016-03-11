package org.jetbrains.haskell.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil


class DoExpression(node : ASTNode) : Expression(node) {
    override fun traverse(visitor: (Expression) -> Unit) {
        visitor(this)
    }

    fun getStatementList() : List<Statement> =
            PsiTreeUtil.getChildrenOfTypeAsList(this, Statement::class.java)
}