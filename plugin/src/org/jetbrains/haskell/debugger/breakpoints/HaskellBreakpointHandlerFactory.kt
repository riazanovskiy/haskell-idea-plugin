package org.jetbrains.haskell.debugger.breakpoints

import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import org.jetbrains.haskell.debugger.HaskellDebugProcess

interface HaskellBreakpointHandlerFactory {
    fun createBreakpointHandler(process: HaskellDebugProcess): XBreakpointHandler<XBreakpoint<*>>
}