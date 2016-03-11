package org.jetbrains.haskell.debugger.prochandlers

import com.intellij.execution.process.ProcessListener
import org.jetbrains.haskell.debugger.procdebuggers.utils.RemoteDebugStreamHandler

/**
 * Created by vlad on 7/30/14.
 */

class RemoteProcessHandler(process: Process, val streamHandler: RemoteDebugStreamHandler) : HaskellDebugProcessHandler(process) {

    init {
        streamHandler.processHandler = this
    }

    override fun setDebugProcessListener(listener: ProcessListener?) {
        streamHandler.listener = listener
    }

    override fun doDestroyProcess() {
        super.doDestroyProcess()
        streamHandler.stop()
    }
}