package org.jetbrains.haskell.debugger.procdebuggers

//import kotlin.test.assertNotNull
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessListener
import org.jetbrains.haskell.debugger.RemoteDebugProcessStateUpdater
import org.jetbrains.haskell.debugger.procdebuggers.utils.DebugRespondent
import org.jetbrains.haskell.debugger.procdebuggers.utils.RemoteDebugStreamHandler
import org.junit.Assert
import java.io.File
import java.util.*

class RemoteDebuggerTest : DebuggerTest<RemoteDebugger>() {

    companion object {
        val pathPropertyName: String = "remotePath"

        class TestRemoteProcessHandler(process: Process, val streamHandler: RemoteDebugStreamHandler,
                                              listener: ProcessListener) : OSProcessHandler(process, null, null) {
            init {
                streamHandler.processHandler = this
                streamHandler.listener = listener
            }

            override fun doDestroyProcess() {
                super.doDestroyProcess()
                streamHandler.stop()
            }
        }
    }

    private var listener: RemoteDebugProcessStateUpdater? = null

    override fun createDebugger(file: File, respondent: DebugRespondent): RemoteDebugger {
        val filePath = file.absolutePath

        val streamHandler = RemoteDebugStreamHandler()
        streamHandler.start()

        val debuggerPath = DebuggerTest.properties?.getProperty(pathPropertyName)
        Assert.assertNotNull(debuggerPath, "Path to remote debugger not found ($pathPropertyName property inside unittest.properties)")

        val command: ArrayList<String> = arrayListOf(debuggerPath!!, "-m$filePath", "-p${streamHandler.getPort()}")
        val builder = ProcessBuilder(command)
        listener = RemoteDebugProcessStateUpdater()
        val handler = TestRemoteProcessHandler(builder.start(), streamHandler, listener!!)
        val debugger = RemoteDebugger(respondent, handler)
        listener!!.debugger = debugger
        return debugger
    }

    override fun stopDebuggerServices() {
        listener?.close()
    }
}