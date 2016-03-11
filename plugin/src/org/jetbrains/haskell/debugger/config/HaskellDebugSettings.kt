package org.jetbrains.haskell.debugger.config

import com.intellij.openapi.components.*
import java.io.File
import org.jetbrains.haskell.util.*


/**
 * Created by vlad on 8/1/14.
 */

@State(
        name = "HaskellDebugConfiguration",
        storages = arrayOf(Storage(id = "default", file = StoragePathMacros.APP_CONFIG + "/haskelldebug.xml")
        )
) class HaskellDebugSettings : PersistentStateComponent<HaskellDebugSettings.Companion.State> {
    companion object {

        class State {
            var debuggerType: DebuggerType = DebuggerType.REMOTE
            var remoteDebuggerPath: String? = null
            var traceOff: Boolean = false
            var printDebugOutput: Boolean = false
        }

        fun getInstance(): HaskellDebugSettings {
            val persisted = ServiceManager.getService(HaskellDebugSettings::class.java)
            if (persisted == null) {
                val settings = HaskellDebugSettings()
                settings.update();
                return settings
            }
            persisted.update();
            return persisted
        }
    }

    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State?) {
        if (state == null) {
            throw RuntimeException("given state is null")
        }
        this.myState = state

        update()
    }

    private fun update() {
        if (myState.remoteDebuggerPath == null || myState.remoteDebuggerPath == "") {
            myState.remoteDebuggerPath = OSUtil.getDefaultCabalBin() + File.separator + OSUtil.getExe("remote-debugger");
        }
    }
}

enum class DebuggerType {
    GHCI,
    REMOTE
}