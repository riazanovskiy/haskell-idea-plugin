package org.jetbrains.haskell.debugger.repl

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import org.jetbrains.haskell.debugger.prochandlers.HaskellDebugProcessHandler


object DebugConsoleFactory {
    fun createDebugConsole(project: Project,
                                  processHandler: HaskellDebugProcessHandler): ConsoleView {
        val console = ConsoleViewImpl(project, false)

        ProcessTerminatedListener.attach(processHandler)
        console.attachToProcess(processHandler)

        return console
    }
}