package org.jetbrains.haskell.debugger.frames

import org.jetbrains.haskell.debugger.parser.HsStackFrameInfo

class ProgramThreadInfo(val id: String?,
                               val name: String,
                               val topFrameInfo: HsStackFrameInfo) {

    enum class State {
        RUNNING,
        SUSPENDED,
        KILLED
    }

    var state: State = State.SUSPENDED
        private set
}