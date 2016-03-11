package org.jetbrains.haskell.debugger.protocol

import org.jetbrains.haskell.debugger.procdebuggers.utils.DebugRespondent
import org.json.simple.JSONObject
import java.util.*

/**
 * @author Habibullin Marat
 */

class RemoveBreakpointCommand(val module: String?, val breakpointNumber: Int, callback: CommandCallback<Nothing?>?)
: RealTimeCommand<Nothing?>(callback) {

    override fun getText(): String = ":delete ${module ?: ""} $breakpointNumber\n"

    override fun parseGHCiOutput(output: Deque<String?>): Nothing? = null

    override fun parseJSONOutput(output: JSONObject): Nothing? = null

    class StandardRemoveBreakpointCallback(val respondent: DebugRespondent) : CommandCallback<Nothing?>() {
        override fun execAfterParsing(result: Nothing?) {
            respondent.breakpointRemoved()
        }

    }
}
