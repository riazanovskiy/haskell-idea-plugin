package org.jetbrains.haskell.debugger

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.impl.actions.ForceStepIntoAction
import com.intellij.xdebugger.impl.actions.StepOutAction
import com.intellij.xdebugger.ui.XDebugTabLayouter
import org.jetbrains.haskell.debugger.breakpoints.HaskellExceptionBreakpointHandler
import org.jetbrains.haskell.debugger.breakpoints.HaskellExceptionBreakpointProperties
import org.jetbrains.haskell.debugger.breakpoints.HaskellLineBreakpointHandler
import org.jetbrains.haskell.debugger.breakpoints.HaskellLineBreakpointType
import org.jetbrains.haskell.debugger.config.DebuggerType
import org.jetbrains.haskell.debugger.config.HaskellDebugSettings
import org.jetbrains.haskell.debugger.highlighting.HsDebugSessionListener
import org.jetbrains.haskell.debugger.parser.BreakInfo
import org.jetbrains.haskell.debugger.parser.LocalBinding
import org.jetbrains.haskell.debugger.procdebuggers.GHCiDebugger
import org.jetbrains.haskell.debugger.procdebuggers.ProcessDebugger
import org.jetbrains.haskell.debugger.procdebuggers.RemoteDebugger
import org.jetbrains.haskell.debugger.procdebuggers.utils.DebugRespondent
import org.jetbrains.haskell.debugger.procdebuggers.utils.DefaultRespondent
import org.jetbrains.haskell.debugger.prochandlers.HaskellDebugProcessHandler
import org.jetbrains.haskell.debugger.protocol.*
import org.jetbrains.haskell.debugger.utils.HaskellUtils
import org.jetbrains.haskell.debugger.utils.SyncObject
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Main class for managing debug process and sending commands to real debug process through it's ProcessDebugger member.
 *
 * Attention! When sending commands to the underlying ProcessDebugger they are enqueued. But some commands may require
 * a lot of time to finish and, for example, if you call asynchronous command that needs much time to finish and
 * after that call synchronous command that freezes UI thread, you will get all the UI frozen until the first
 * command is finished. To check no command is in progress use
 * {@link org.jetbrains.haskell.debugger.HaskellDebugProcess#isReadyForNextCommand}
 *
 * @see org.jetbrains.haskell.debugger.HaskellDebugProcess#isReadyForNextCommand
 */

class HaskellDebugProcess(session: XDebugSession,
                                 val executionConsole: ExecutionConsole,
                                 val _processHandler: HaskellDebugProcessHandler,
                                 val stopAfterTrace: Boolean) : XDebugProcess(session) {

    //public val historyManager: HistoryManager = HistoryManager(session , this)
    var exceptionBreakpoint: XBreakpoint<HaskellExceptionBreakpointProperties>? = null
        private set
    val debugger: ProcessDebugger

    private val debugRespondent: DebugRespondent = DefaultRespondent(this)
    private val contexts: Deque<XSuspendContext> = ArrayDeque()
    private val debugProcessStateUpdater: DebugProcessStateUpdater
    private val _editorsProvider: XDebuggerEditorsProvider = HaskellDebuggerEditorsProvider()
    private val _breakpointHandlers: Array<XBreakpointHandler<*>> = arrayOf(HaskellLineBreakpointHandler(getSession()!!.project, HaskellLineBreakpointType::class.java, this),
            HaskellExceptionBreakpointHandler(this)
    )
    private val registeredBreakpoints: MutableMap<BreakpointPosition, BreakpointEntry> = hashMapOf()

    private val BREAK_BY_INDEX_ERROR_MSG = "Only remote debugger supports breakpoint setting by index"

    init {
        val debuggerIsGHCi = HaskellDebugSettings.getInstance().state.debuggerType == DebuggerType.GHCI
        if (debuggerIsGHCi) {
            debugProcessStateUpdater = GHCiDebugProcessStateUpdater()
            debugger = GHCiDebugger(debugRespondent, _processHandler,
                    executionConsole as ConsoleView,
                    debugProcessStateUpdater.INPUT_READINESS_PORT)
            debugProcessStateUpdater.debugger = debugger
        } else {
            debugProcessStateUpdater = RemoteDebugProcessStateUpdater()
            debugger = RemoteDebugger(debugRespondent, _processHandler)
            debugProcessStateUpdater.debugger = debugger
        }
        _processHandler.setDebugProcessListener(debugProcessStateUpdater)
    }

    // XDebugProcess methods overriding

    override fun getEditorsProvider(): XDebuggerEditorsProvider = _editorsProvider

    override fun getBreakpointHandlers()
            : Array<XBreakpointHandler<out XBreakpoint<out XBreakpointProperties<*>?>?>> = _breakpointHandlers

    override fun doGetProcessHandler(): ProcessHandler? = _processHandler

    override fun createConsole(): ExecutionConsole = executionConsole

    override fun startStepOver() = debugger.stepOver()

    override fun startStepInto() = debugger.stepInto()

    override fun startStepOut() {
        val msg = "'Step out' not implemented"
        Notifications.Bus.notify(Notification("", "Debug execution error", msg, NotificationType.WARNING))
        session!!.positionReached(session!!.suspendContext!!)
    }

    override fun stop() {
        //historyManager.clean()
        debugger.close()
        debugProcessStateUpdater.close()
    }

    override fun resume() = debugger.resume()

    override fun runToPosition(position: XSourcePosition) =
            debugger.runToPosition(
                    HaskellUtils.getModuleName(session!!.project, position.file),
                    HaskellUtils.zeroBasedToHaskellLineNumber(position.line))

    override fun sessionInitialized() {
        super.sessionInitialized()
        val currentSession = session
        currentSession?.addSessionListener(HsDebugSessionListener(currentSession))
        debugger.prepareDebugger()
        if (stopAfterTrace) {
            debugger.trace(null)
        }
    }

    override fun createTabLayouter(): XDebugTabLayouter = object : XDebugTabLayouter() {
        override fun registerAdditionalContent(ui: RunnerLayoutUi) {
            //historyManager.registerContent(ui)
        }
    }

    override fun registerAdditionalActions(leftToolbar: DefaultActionGroup,
                                           topToolbar: DefaultActionGroup,
                                           settings: DefaultActionGroup) {
        //temporary code for removal of unused actions from debug panel
        var stepOut: StepOutAction? = null
        var forceStepInto: ForceStepIntoAction? = null
        for (action in topToolbar.childActionsOrStubs) {
            if (action is StepOutAction) {
                stepOut = action
            }
            if (action is ForceStepIntoAction) {
                forceStepInto = action
            }
        }
        topToolbar.remove(stepOut)
        topToolbar.remove(forceStepInto)

        //historyManager.registerActions(topToolbar)
    }

    // Class' own methods
    fun startTrace(line: String?) {
        //historyManager.saveState()
        val context = session!!.suspendContext
        if (context != null) {
            contexts.add(context)
        }
        // disable actions
        debugger.trace(line)
    }

    fun traceFinished() {
        /*
        if (historyManager.hasSavedStates()) {
            historyManager.loadState()
            if (!contexts.isEmpty()) {
                getSession()!!.positionReached(contexts.pollLast()!!)
            }
        } else if (stopAfterTrace) {
            getSession()!!.stop()
        } else {

        }
        */
    }

    fun isReadyForNextCommand(): Boolean = debugger.isReadyForNextCommand()

    fun addExceptionBreakpoint(breakpoint: XBreakpoint<HaskellExceptionBreakpointProperties>) {
        exceptionBreakpoint = breakpoint
        debugger.setExceptionBreakpoint(breakpoint.properties!!.state.exceptionType ==
                HaskellExceptionBreakpointProperties.ExceptionType.ERROR)
    }

    fun removeExceptionBreakpoint(breakpoint: XBreakpoint<HaskellExceptionBreakpointProperties>) {
        assert(breakpoint == exceptionBreakpoint)
        exceptionBreakpoint = null
        debugger.removeExceptionBreakpoint()
    }

    fun setBreakpointNumberAtLine(breakpointNumber: Int, module: String, line: Int) {
        val entry = registeredBreakpoints[BreakpointPosition(module, line)]
        if (entry != null) {
            entry.breakpointNumber = breakpointNumber
        }
    }

    fun getBreakpointAtPosition(module: String, line: Int): XLineBreakpoint<XBreakpointProperties<*>>? =
            registeredBreakpoints[BreakpointPosition(module, line)]?.breakpoint

    fun addBreakpoint(module: String, line: Int, breakpoint: XLineBreakpoint<XBreakpointProperties<*>>) {
        registeredBreakpoints.put(BreakpointPosition(module, line), BreakpointEntry(null, breakpoint))
        debugger.setBreakpoint(module, line)
    }

    fun addBreakpointByIndex(module: String, index: Int, breakpoint: XLineBreakpoint<XBreakpointProperties<*>>) {
        if (HaskellDebugSettings.getInstance().state.debuggerType == DebuggerType.REMOTE) {
            val line = HaskellUtils.zeroBasedToHaskellLineNumber(breakpoint.line)
            registeredBreakpoints.put(BreakpointPosition(module, line), BreakpointEntry(index, breakpoint))
            val command = SetBreakpointByIndexCommand(module, index, SetBreakpointCommand.Companion.StandardSetBreakpointCallback(module, debugRespondent))
            debugger.enqueueCommand(command)
        } else {
            throw RuntimeException(BREAK_BY_INDEX_ERROR_MSG)
        }
    }

    fun removeBreakpoint(module: String, line: Int) {
        val breakpointNumber: Int? = registeredBreakpoints[BreakpointPosition(module, line)]?.breakpointNumber
        if (breakpointNumber != null) {
            registeredBreakpoints.remove(BreakpointPosition(module, line))
            debugger.removeBreakpoint(module, breakpointNumber)
        }
    }

    fun forceSetValue(localBinding: LocalBinding) {
        if (localBinding.name != null) {
            val syncObject: Lock = ReentrantLock()
            val bindingValueIsSet: Condition = syncObject.newCondition()
            val syncLocalBinding: LocalBinding = LocalBinding(localBinding.name, "", null)
            syncObject.lock()
            try {

                debugger.force(localBinding.name!!,
                        ForceCommand.StandardForceCallback(syncLocalBinding, syncObject, bindingValueIsSet, this))

                while (syncLocalBinding.value == null) {
                    bindingValueIsSet.await()
                }
                if (syncLocalBinding.value?.isNotEmpty() ?: false) {
                    localBinding.value = syncLocalBinding.value
                }
            } finally {
                syncObject.unlock()
            }
        }
    }

    fun syncBreakListForLine(moduleName: String, lineNumber: Int): ArrayList<BreakInfo> {
        if (HaskellDebugSettings.getInstance().state.debuggerType == DebuggerType.REMOTE) {
            val syncObject = SyncObject()
            val resultArray: ArrayList<BreakInfo> = ArrayList()
            val callback = BreakpointListCommand.Companion.DefaultCallback(resultArray)
            val command = BreakpointListCommand(moduleName, lineNumber, syncObject, callback)
            syncCommand(command, syncObject)
            return resultArray
        }
        return ArrayList()
    }

    private class BreakpointPosition(val module: String, val line: Int) {
        override fun equals(other: Any?): Boolean {
            if (other == null || other !is BreakpointPosition) {
                return false
            }
            return module.equals(other.module) && line.equals(other.line)
        }

        override fun hashCode(): Int = module.hashCode() * 31 + line
    }

    private class BreakpointEntry(var breakpointNumber: Int?, val breakpoint: XLineBreakpoint<XBreakpointProperties<*>>)

    /**
     * Used to make synchronous requests to debugger.
     *
     * @see org.jetbrains.haskell.debugger.utils.SyncObject
     * @see org.jetbrains.haskell.debugger.HaskellDebugProcess#isReadyForNextCommand
     */
    private fun syncCommand(command: SyncCommand<*>, syncObject: SyncObject) {
        syncObject.lock()
        try {
            debugger.enqueueCommand(command)
            while (!syncObject.signaled()) {
                syncObject.await()
            }
        } finally {
            syncObject.unlock()
        }
    }
}