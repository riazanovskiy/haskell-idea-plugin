package org.jetbrains.haskell.debugger.protocol

import org.jetbrains.haskell.debugger.parser.EvalResult
import org.jetbrains.haskell.debugger.parser.JSONConverter
import org.json.simple.JSONObject
import java.util.*

/**
 * Created by vlad on 8/1/14.
 */
class EvalCommand(val force: Boolean, val expression: String, callback: CommandCallback<EvalResult?>)
: RealTimeCommand<EvalResult?>(callback) {

    override fun getText(): String = ":eval ${if (force) 1 else 0} ${expression.trim()}\n"

    override fun parseGHCiOutput(output: Deque<String?>) = null

    override fun parseJSONOutput(output: JSONObject): EvalResult? =
            if (JSONConverter.checkExceptionFromJSON(output) == null) JSONConverter.evalResultFromJSON(output) else null
}