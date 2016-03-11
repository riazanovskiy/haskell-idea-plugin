package org.jetbrains.haskell.debugger.protocol

import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import org.jetbrains.haskell.debugger.frames.HsDebugValue
import org.jetbrains.haskell.debugger.parser.GHCiParser
import org.jetbrains.haskell.debugger.parser.LocalBinding
import org.jetbrains.haskell.debugger.parser.ShowOutput
import org.json.simple.JSONObject
import java.util.*

/**
 * Created by vlad on 7/23/14.
 */

class ShowExpressionCommand(val expression: String, callback: CommandCallback<ShowOutput?>)
: RealTimeCommand<ShowOutput?>(callback) {

    /*
     * 'show' may be hidden
     */
    override fun getText(): String = "Prelude.show (${expression.trim()})\n"

    override fun parseGHCiOutput(output: Deque<String?>): ShowOutput? = GHCiParser.tryParseShowOutput(output)

    override fun parseJSONOutput(output: JSONObject): ShowOutput? {
        throw RuntimeException("Unused in remote debugger")
    }

    companion object {
        class StandardShowExpressionCallback(val expressionType: String?, val callback: XDebuggerEvaluator.XEvaluationCallback)
        : CommandCallback<ShowOutput?>() {
            override fun execAfterParsing(result: ShowOutput?) {
                if (result == null) {
                    callback.errorOccurred("Cannot show type: $expressionType")
                } else if (result is ShowOutput) {
                    callback.evaluated(HsDebugValue(LocalBinding(null, expressionType, result.output)))
                }
            }
        }
    }
}