package org.jetbrains.haskell.debugger.frames

//import org.jetbrains.haskell.debugger.protocol.ExpressionCommand
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import org.jetbrains.haskell.debugger.procdebuggers.ProcessDebugger

/**
 * Created by vlad on 7/23/14.
 */

class HsDebuggerEvaluator (val debugger: ProcessDebugger): XDebuggerEvaluator() {

    override fun evaluate(expression: String, callback: XDebuggerEvaluator.XEvaluationCallback, expressionPosition: XSourcePosition?) {
        debugger.evaluateExpression(expression, callback)
    }
}