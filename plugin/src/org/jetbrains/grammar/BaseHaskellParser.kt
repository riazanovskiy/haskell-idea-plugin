package org.jetbrains.grammar

import com.intellij.psi.tree.IElementType
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import org.jetbrains.grammar.dumb.Rule
import com.intellij.lang.ASTNode
import org.jetbrains.haskell.parser.rules.BaseParser
import java.util.ArrayList
import org.jetbrains.grammar.dumb.NonTerminalTree

import org.jetbrains.grammar.dumb.TerminalTree

import org.jetbrains.grammar.dumb.Variant
import org.jetbrains.grammar.dumb.Term
import org.jetbrains.grammar.dumb.LazyLLParser
import org.jetbrains.haskell.parser.getCachedTokens
import org.jetbrains.haskell.parser.token.NEW_LINE
import org.jetbrains.grammar.dumb.Terminal
import org.jetbrains.haskell.parser.HaskellTokenType
import org.jetbrains.grammar.dumb.NonTerminal
import org.jetbrains.grammar.dumb.TerminalVariant
import org.jetbrains.grammar.dumb.NonTerminalVariant


abstract class BaseHaskellParser(val builder: PsiBuilder?) {

    companion object {
        var cache : Map<String, Rule>? = null;
    }

    abstract fun getGrammar() : Map<String, Rule>

    fun mark() : Marker {
        return builder!!.mark()
    }

    fun parse(root: IElementType): ASTNode {

        val marker = builder!!.mark()
        val cachedTokens = getCachedTokens(builder)
        marker.rollbackTo();

        val rootMarker = mark()

        if (cache == null) {
            val grammar = getGrammar()
            findFirst(grammar)
            cache = grammar
        }
        val tree = LazyLLParser(cache!!, cachedTokens).parse()

        if (tree != null) {
            parserWithTree(tree)
        }

        while (!builder.eof()) {
            builder.advanceLexer()
        }
        rootMarker.done(root)
        return builder.getTreeBuilt()
    }

    fun parserWithTree(tree: NonTerminalTree) {
        val type = tree.elementType

        val builderNotNull = builder!!
        val marker = if (type != null) builderNotNull.mark() else null

        for (child in tree.children) {
            when (child) {
                is NonTerminalTree -> parserWithTree(child)
                is TerminalTree -> {
                    if (child.haskellToken != HaskellLexerTokens.VOCURLY &&
                        child.haskellToken != HaskellLexerTokens.VCCURLY) {
                        if (child.haskellToken == builderNotNull.getTokenType()) {
                            builderNotNull.advanceLexer()
                        } else if (child.haskellToken != HaskellLexerTokens.SEMI) {
                            throw RuntimeException()
                        }
                    }
                }
            }
        }

        marker?.done(type!!)
    }

    fun findFirst(grammar : Map<String, Rule>) {
        for (rule in grammar.values) {
            rule.makeAnalysis(grammar);
        }
        for (rule in grammar.values) {
            rule.makeDeepAnalysis(grammar);
        }
    }

    fun end(): TerminalVariant {
        return TerminalVariant(null)
    }

    fun end(elementType: IElementType): TerminalVariant {
        return TerminalVariant(elementType)
    }

    fun many(str : String, vararg next : Variant): NonTerminalVariant {
        val list = next.toMutableList()
        return NonTerminalVariant(NonTerminal(str), list)
    }

    fun many(tokenType : HaskellTokenType, vararg next : Variant): NonTerminalVariant {
        val list = next.toMutableList()
        return NonTerminalVariant(Terminal(tokenType), list)
    }

    fun nonTerm(rule : String): NonTerminal {
        return NonTerminal(rule)
    }

    fun addVar(variants : MutableList<Variant>, variant : Variant): Variant {
        variants.add(variant);
        return variant;
    }
}