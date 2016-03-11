package org.jetbrains.haskell.debugger.prochandlers

import com.intellij.execution.process.ProcessListener

class GHCiProcessHandler(process: Process) : HaskellDebugProcessHandler(process) {

    override fun setDebugProcessListener(listener: ProcessListener?) {
        addProcessListener(listener)
    }

}