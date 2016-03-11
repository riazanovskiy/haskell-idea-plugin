package org.jetbrains.cabal.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.cabal.CabalFile
import org.jetbrains.cabal.CabalLanguage

class CabalParserDefinition() : ParserDefinition {
    var CABAL_FILE: IFileElementType = IFileElementType(CabalLanguage.INSTANCE)


    override fun createLexer(project: Project?): Lexer {
        return CabalLexer()
    }

    override fun getFileNodeType(): IFileElementType {
        return CABAL_FILE
    }

    override fun getWhitespaceTokens(): TokenSet {
        return CabalTokelTypes.WHITESPACES
    }

    override fun getCommentTokens(): TokenSet {
        return CabalTokelTypes.COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.create(CabalTokelTypes.STRING)
    }

    override fun createParser(project: Project?): PsiParser {
        return PsiParser { root, builder -> CabalParser(root, builder).parse() }
    }

    override fun createFile(viewProvider: FileViewProvider?): PsiFile {
        return CabalFile(viewProvider!!)
    }

    override fun spaceExistanceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode?): PsiElement {
        if (node!!.elementType is CabalCompositeElementType) {
            return ((node.elementType as CabalCompositeElementType)).contructor(node)
        } else {
            return ASTWrapperPsiElement(node)
        }
    }
}
