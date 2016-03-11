package org.jetbrains.haskell.debugger.procdebuggers

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import org.jetbrains.haskell.debugger.parser.*
import org.jetbrains.haskell.debugger.procdebuggers.utils.DebugRespondent
import org.jetbrains.haskell.debugger.protocol.*

abstract class SimpleDebuggerImpl(val debugRespondent: DebugRespondent,
                                         debugProcessHandler: ProcessHandler,
                                         consoleView: ConsoleView?) : QueueDebugger(debugProcessHandler, consoleView) {

    /**
     * Function, which is used to run with ':trace' command.
     */
    protected val TRACE_COMMAND: String = "main"

    /**
     * When true, all breakpoint indices for all files are unique,
     * when false, breakpoint indices are unique only within one file.
     * Value is used to determine correct :delete invocation.
     */
    protected abstract val GLOBAL_BREAKPOINT_INDICES: Boolean

    protected open fun fixTraceCommand(line: String): String = line

    override fun trace(line: String?) = enqueueCommand(TraceCommand(fixTraceCommand(line ?: TRACE_COMMAND),
            FlowCommand.StandardFlowCallback(this, debugRespondent)))

    override fun stepInto() = enqueueCommand(StepIntoCommand(StepCommand.StandardStepCallback(this, debugRespondent)))

    override fun stepOver() = enqueueCommand(StepOverCommand(StepCommand.StandardStepCallback(this, debugRespondent)))

    override fun resume() = enqueueCommand(ResumeCommand(FlowCommand.StandardFlowCallback(this, debugRespondent)))

    override fun back(callback: CommandCallback<MoveHistResult?>?) = enqueueCommand(BackCommand(callback))

    override fun forward(callback: CommandCallback<MoveHistResult?>?) = enqueueCommand(ForwardCommand(callback))

    override fun print(binding: String, printCallback: CommandCallback<LocalBinding?>) =
            enqueueCommand(PrintCommand(binding, printCallback))

    override fun force(binding: String, forceCallback: CommandCallback<LocalBinding?>) =
            enqueueCommand(ForceCommand(binding, forceCallback))

    override fun history(callback: CommandCallback<HistoryResult?>) = enqueueCommand(HistoryCommand(callback))

    override fun setBreakpoint(module: String, line: Int) {
        val callback = SetBreakpointCommand.Companion.StandardSetBreakpointCallback(module, debugRespondent)
        enqueueCommand(SetBreakpointCommand(module, line, callback))
    }

    override fun removeBreakpoint(module: String, breakpointNumber: Int) {
        val moduleName = if (GLOBAL_BREAKPOINT_INDICES) null else module
        enqueueCommand(RemoveBreakpointCommand(moduleName, breakpointNumber,
                RemoveBreakpointCommand.StandardRemoveBreakpointCallback(debugRespondent)))
    }

    override fun setExceptionBreakpoint(uncaughtOnly: Boolean) =
            enqueueCommand(HiddenCommand.createInstance(":set -fbreak-on-${if (uncaughtOnly) "error" else "exception"}\n"))

    override fun removeExceptionBreakpoint() {
        enqueueCommand(HiddenCommand.createInstance(":unset -fbreak-on-error\n"))
        enqueueCommand(HiddenCommand.createInstance(":unset -fbreak-on-exception\n"))
    }

    override fun runToPosition(module: String, line: Int) {
        if (debugRespondent.getBreakpointAt(module, line) == null) {
            val callback = SetTempBreakForRunCallback(if (GLOBAL_BREAKPOINT_INDICES) null else module)
            enqueueCommand(SetBreakpointCommand(module, line, callback))
        } else {
            if (debugStarted) resume() else trace(null)
        }
    }

    protected inner class SetTempBreakForRunCallback(val module: String?)
    : CommandCallback<BreakpointCommandResult?>() {
        override fun execAfterParsing(result: BreakpointCommandResult?) {
            if (result != null) {
                val callback = RunToPositionCallback(result.breakpointNumber, module)
                val command = if (debugStarted) ResumeCommand(callback) else TraceCommand(fixTraceCommand("main"), callback)
                enqueueCommandWithPriority(command)
            }
        }
    }

    private inner class RunToPositionCallback(val breakpointNumber: Int,
                                              val module: String?) : CommandCallback<HsStackFrameInfo?>() {
        override fun execAfterParsing(result: HsStackFrameInfo?) {
            val command = RemoveBreakpointCommand(module, breakpointNumber, RemoveTempBreakCallback(result))
            enqueueCommandWithPriority(command)
        }
    }

    private inner class RemoveTempBreakCallback(val flowResult: HsStackFrameInfo?)
    : CommandCallback<Nothing?>() {
        override fun execAfterParsing(result: Nothing?) =
                FlowCommand.StandardFlowCallback(this@SimpleDebuggerImpl, debugRespondent).execAfterParsing(flowResult)
    }
}