package org.jetbrains.haskell.debugger.protocol

import java.util.Deque
import org.jetbrains.haskell.debugger.parser.ParseResult
import org.json.simple.JSONObject

/**
 * Created by vlad on 7/17/14.
 */

abstract class HiddenCommand
: AbstractCommand<ParseResult?>(null) {

    companion object {
        fun createInstance(command: String): HiddenCommand {
            return object : HiddenCommand() {
                override fun getText(): String = command
            }
        }
    }

    override fun parseGHCiOutput(output: Deque<String?>): ParseResult? = null

    override fun parseJSONOutput(output: JSONObject): ParseResult? = null
}
