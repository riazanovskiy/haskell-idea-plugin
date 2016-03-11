package org.jetbrains.haskell.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.grammar.HaskellLexerTokens
import org.jetbrains.haskell.HaskellLanguage
import org.jetbrains.haskell.fileType.HaskellFile
import org.jetbrains.haskell.parser.lexer.HaskellLexer
import org.jetbrains.haskell.parser.token.COMMENTS
import org.jetbrains.haskell.parser.token.WHITESPACES


class HaskellParserDefinition() : ParserDefinition {
    val HASKELL_FILE = IFileElementType(HaskellLanguage.INSTANCE)

    override fun createLexer(project: Project?): Lexer = HaskellLexer()

    override fun getFileNodeType(): IFileElementType = HASKELL_FILE

    override fun getWhitespaceTokens() = WHITESPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(HaskellLexerTokens.STRING)


    override fun createParser(project: Project?): PsiParser =
            PsiParser { root, builder -> org.jetbrains.grammar.HaskellParser(builder).parse(root) }

    override fun createFile(viewProvider: FileViewProvider?): PsiFile =
        HaskellFile(viewProvider!!)


    override fun spaceExistanceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) =
        ParserDefinition.SpaceRequirements.MAY


    override fun createElement(node: ASTNode?): PsiElement {
        val elementType = node!!.elementType
        if (elementType is HaskellCompositeElementType) {
            val constructor = elementType.constructor
            if (constructor != null) {
                return constructor(node)
            }
        }
        return ASTWrapperPsiElement(node)
    }

}
