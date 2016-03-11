package org.jetbrains.haskell.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.jetbrains.grammar.HaskellLexerTokens.*
import org.jetbrains.haskell.psi.*


/**
 * Created by atsky on 6/6/14.
 */
class HaskellAnnotator() : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is Import) {
            for (node in element.node.getChildren(TokenSet.create(HIDING, QUALIFIED))) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_KEYWORD
            }
            for (node in element.getImportAsPart()?.node?.getChildren(TokenSet.create(AS)) ?: arrayOf()) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_KEYWORD
            }
        }
        if (element is FunctionType) {
            for (node in element.node.getChildren(TokenSet.create(RARROW))) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_TYPE
            }
        }
        if (element is ListType) {
            for (node in element.node.getChildren(TokenSet.create(OBRACK, CBRACK))) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_TYPE
            }
        }
        if (element is QVarSym) {
            for (node in element.node.getChildren(TokenSet.create(OPAREN, CPAREN))) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_OPERATOR
            }
        }
        if (element is TypeVariable && !element.isConstructor() && !element.isClass()) {
            for (node in element.node.getChildren(TokenSet.create(CONID, VARID, QCONID, QVARID))) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_TYPE
            }
        }
        if (element is VariableOperation) {
            for (node in element.node.getChildren(null)) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_OPERATOR
            }
        }
        if (element is SignatureDeclaration) {
            val qVar = element.getQNameExpression()?.getQVar()
            val node = qVar?.node?.firstChildNode
            if (node != null) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_SIGNATURE
            }
        }
        if (element is TupleType && element.getParent() !is Context) {
            for (node in element.node.getChildren(TokenSet.create(OPAREN, CPAREN, COMMA))) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_TYPE
            }
        }
        if (element is Context) {
            for (node in element.node.getChildren(null)) {
                holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_CLASS
            }
        }
        if (element is ClassDeclaration) {
            val haskellType = element.getType()
            if (haskellType != null) {
                for (node in haskellType.node.getChildren(null)) {
                    holder.createInfoAnnotation(node, null)?.textAttributes = HaskellHighlighter.HASKELL_CLASS
                }
            }
        }
    }

}