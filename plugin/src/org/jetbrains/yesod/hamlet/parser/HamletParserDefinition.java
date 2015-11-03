package org.jetbrains.yesod.hamlet.parser;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yesod.hamlet.HamletFile;
import org.jetbrains.yesod.hamlet.HamletLanguage;


public class HamletParserDefinition implements ParserDefinition {

    public static IFileElementType HAMLET_FILE  = new IFileElementType(HamletLanguage.INSTANCE);
    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new HamletLexer();
    }

    @Override
    public PsiParser createParser(Project project) {
        return new HamletParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return HAMLET_FILE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return HamletTokenTypes.WHITESPACES;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode astNode) {
        return new ASTWrapperPsiElement(astNode);
    }

    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new HamletFile(fileViewProvider);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode2) {
        return ParserDefinition.SpaceRequirements.MAY;
    }
}
