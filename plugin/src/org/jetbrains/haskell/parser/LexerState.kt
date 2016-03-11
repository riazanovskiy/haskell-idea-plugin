package org.jetbrains.haskell.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.jetbrains.grammar.HaskellLexerTokens
import org.jetbrains.haskell.parser.lexer.HaskellLexer
import org.jetbrains.haskell.parser.token.COMMENTS
import org.jetbrains.haskell.parser.token.NEW_LINE
import java.io.PrintStream
import java.util.*

val INDENT_TOKENS = HashSet<IElementType>(Arrays.asList(
        HaskellLexerTokens.DO,
        HaskellLexerTokens.OF,
        HaskellLexerTokens.LET,
        HaskellLexerTokens.LCASE, // In GHC this depends on extension LambdaCase.
        HaskellLexerTokens.WHERE))

class IntStack(val indent: Int,
               val parent: IntStack?)

fun getCachedTokens(lexer: HaskellLexer, stream: PrintStream?): CachedTokens {
    val tokens = ArrayList<IElementType>()
    val starts = ArrayList<Int>()
    val indents = ArrayList<Int>()
    val lineStarts = ArrayList<Boolean>()

    var currentIndent = 0
    var isLineStart = true

    stream?.println("-------------------")
    while (lexer.tokenType != null) {
        val tokenType = lexer.tokenType!!
        if (!COMMENTS.contains(tokenType) && tokenType != TokenType.WHITE_SPACE) {
            if (tokenType == NEW_LINE) {
                currentIndent = 0
                isLineStart = true
                stream?.println()
            } else {
                tokens.add(tokenType)
                starts.add(lexer.tokenStart)
                indents.add(currentIndent)
                lineStarts.add(isLineStart)
                isLineStart = false
                stream?.print("$tokenType ")
            }
        }

        if (tokenType != NEW_LINE) {
            for (ch in lexer.tokenText) {
                if (ch == '\t') {
                    currentIndent += 8;
                } else {
                    currentIndent += 1;
                }
            }
        }
        lexer.advance();
    }
    stream?.println("-------------------")
    return CachedTokens(tokens, starts, indents, lineStarts)
}

fun getCachedTokens(builder: PsiBuilder): CachedTokens {
    val tokens = ArrayList<IElementType>()
    val starts = ArrayList<Int>()
    val indents = ArrayList<Int>()
    val lineStarts = ArrayList<Boolean>()

    var currentIndent = 0
    var isLineStart = true

    builder.setWhitespaceSkippedCallback({ type, start, end ->
        if (type == NEW_LINE) {
            currentIndent = 0
            isLineStart = true
        } else {
            val charSequence = builder.originalText
            for (i in start..(end-1)) {
                if (charSequence[i] == '\t') {
                    currentIndent += 8;
                } else {
                    currentIndent += 1;
                }
            }
        }
    })

    while (builder.tokenType != null) {
        tokens.add(builder.tokenType!!)
        starts.add(builder.currentOffset)
        indents.add(currentIndent)
        lineStarts.add(isLineStart)
        isLineStart = false

        currentIndent += builder.tokenText!!.length

        builder.advanceLexer()
    }

    return CachedTokens(tokens, starts, indents, lineStarts)
}

fun newLexerState(tokens: CachedTokens): LexerState {
    if (tokens.tokens.firstOrNull() == HaskellLexerTokens.MODULE) {
        return LexerState(tokens, 0, 0, null, null)
    } else {
        return LexerState(tokens, 0, 0, null, IntStack(0, null))
    }
}

class CachedTokens(val tokens: List<IElementType>,
                          val starts: List<Int>,
                          val indents: ArrayList<Int>,
                          val lineStart: ArrayList<Boolean>) {
}

class LexerState(val tokens: CachedTokens,
                        val position: Int,
                        val readedLexemNumber: Int,
                        val currentToken: HaskellTokenType?,
                        val indentStack: IntStack?) {

    fun match(token: HaskellTokenType): Boolean {
        if (currentToken != null) {
            return currentToken == token
        }
        if (position < tokens.tokens.size && tokens.tokens[position] == token) {
            return true
        }
        return false
    }

    fun next(): LexerState {
        if (currentToken != null) {
            if (currentToken == HaskellLexerTokens.VCCURLY && indentStack != null) {
                return checkIndent(position)
            }
            return LexerState(tokens, position, readedLexemNumber + 1, null, indentStack)
        }
        if (position == tokens.tokens.size) {
            return last()
        }
        if (tokens.tokens[position] == HaskellLexerTokens.OCURLY) {
            if (tokens.tokens[position + 1] != HaskellLexerTokens.CCURLY) {
                return LexerState(tokens,
                        position + 1,
                        readedLexemNumber + 1,
                        null,
                        IntStack(-1, indentStack))
            } else {
                return LexerState(tokens,
                        position + 1,
                        readedLexemNumber + 1,
                        null,
                        indentStack)
            }
        }
        val nextPosition = position + 1

        if (nextPosition == tokens.tokens.size) {
            return last()
        }

        if (INDENT_TOKENS.contains(tokens.tokens[position]) &&
                tokens.tokens[nextPosition] != HaskellLexerTokens.OCURLY) {

            val indent = tokens.indents[nextPosition]
            return LexerState(tokens,
                    nextPosition,
                    readedLexemNumber + 1,
                    HaskellLexerTokens.VOCURLY,
                    IntStack(indent, indentStack))
        }

        return checkIndent(nextPosition)
    }

    private fun last(): LexerState {
        if (indentStack != null) {
            return LexerState(tokens,
                    tokens.tokens.size,
                    readedLexemNumber + 1,
                    HaskellLexerTokens.VCCURLY,
                    indentStack.parent)
        } else {
            return LexerState(tokens, tokens.tokens.size, readedLexemNumber, null, null)
        }
    }

    private fun checkIndent(position: Int): LexerState {
        if (position == tokens.tokens.size) {
            return last()
        }
        if (tokens.lineStart[position]) {
            val indent = tokens.indents[position]
            if (indentStack != null) {
                if (indentStack.indent == indent) {
                    return LexerState(tokens, position, readedLexemNumber + 1, HaskellLexerTokens.SEMI, indentStack)
                } else if (indentStack.indent < indent) {
                    return checkCurly(position)
                } else {
                    return LexerState(tokens, position, readedLexemNumber + 1, HaskellLexerTokens.VCCURLY, indentStack.parent)
                }
            } else {
                //if (0 == indent) {
                //    return LexerState(tokens, position, lexemNumber + 1, HaskellLexerTokens.SEMI, indentStack)
                //} else {
                //    return checkCurly(position)
                //}
            }
        }
        return checkCurly(position)
    }

    private fun checkCurly(nextPosition: Int): LexerState {
        if (tokens.tokens[nextPosition] == HaskellLexerTokens.CCURLY) {
            if (indentStack!!.indent > -1) {
                return LexerState(tokens, nextPosition, readedLexemNumber + 1, HaskellLexerTokens.VCCURLY, indentStack.parent)
            }
            return LexerState(tokens, nextPosition, readedLexemNumber + 1, null, indentStack.parent)
        }
        return LexerState(tokens, nextPosition, readedLexemNumber + 1, null, indentStack)
    }

    fun dropIndent() = LexerState(
            tokens,
            position,
            readedLexemNumber + 1,
            HaskellLexerTokens.VCCURLY,
            indentStack?.parent)

    fun getToken(): IElementType? {
        if (currentToken != null) {
            return currentToken
        }
        if (position < tokens.tokens.size) {
            return tokens.tokens[position];
        }
        return null;
    }

    fun eof(): Boolean {
        return currentToken == null && position == tokens.tokens.size;
    }


}