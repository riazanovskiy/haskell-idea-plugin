package org.jetbrains.grammar.dumb

/**
 * Created by atsky on 23/11/14.
 */
abstract class ParserState() {
    abstract fun next() : ParserState;
}

class FinalState(val result : NonTerminalTree?) : ParserState() {
    override fun next(): ParserState {
        return this;
    }

}

