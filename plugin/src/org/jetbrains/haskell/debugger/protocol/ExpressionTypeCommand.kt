package org.jetbrains.haskell.debugger.protocol

import org.jetbrains.haskell.debugger.parser.ExpressionType
import org.jetbrains.haskell.debugger.parser.GHCiParser
import org.json.simple.JSONObject
import java.util.*

/**
 * Created by vlad on 7/23/14.
 */

class ExpressionTypeCommand(val expression: String, callback: CommandCallback<ExpressionType?>?)
: RealTimeCommand<ExpressionType?>(callback) {
    override fun getText(): String = ":type $expression\n"

    override fun parseGHCiOutput(output: Deque<String?>): ExpressionType? = GHCiParser.parseExpressionType(output.first!!)

    override fun parseJSONOutput(output: JSONObject): ExpressionType? {
        throw UnsupportedOperationException()
    }
}