package org.jetbrains.haskell.external

import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.haskell.config.HaskellConfigurable
import org.jetbrains.haskell.config.HaskellSettings
import org.jetbrains.haskell.external.tool.GhcModConsole
import org.jetbrains.haskell.sdk.HaskellSdkType
import org.jetbrains.haskell.util.OSUtil
import org.jetbrains.haskell.util.ProcessRunner
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*
import javax.swing.event.HyperlinkEvent

/**
 * Created by atsky on 15/06/14.
 */
class GhcModi(val project: Project, val settings: HaskellSettings) : ProjectComponent {
    var process: Process? = null;

    override fun projectOpened() {}

    override fun projectClosed() {
        val process = process
        if (process != null) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously({
                synchronized(process) {
                    val output = OutputStreamWriter(process.outputStream!!)
                    output.write("\n")
                    output.close()
                    process.waitFor()
                }
            }, "stopping ghc-modi", false, project)

            this.process = null
        }
    }

    fun startProcess() {
        assert(process == null)
        val sdk = ProjectRootManager.getInstance(project).projectSdk
        val ghcHome = if (sdk != null && sdk.sdkType is HaskellSdkType) {
            sdk.homePath + File.separator + "bin"
        } else {
            null
        }
        process = ProcessRunner(project.baseDir!!.path).getProcess(listOf(getPath()), ghcHome)
        GhcModConsole.getInstance(project).append("start ${getPath()}\n", GhcModConsole.MessageType.INFO)
    }

    fun getPath(): String {
        return settings.state.ghcModiPath!!
    }


    override fun initComponent() {

    }

    override fun disposeComponent() {

    }

    override fun getComponentName(): String = "ghc-modi"

    fun isStopped(): Boolean {
        try {
            process!!.exitValue()
            return true
        } catch(e: IllegalThreadStateException) {
            return false
        }
    }

    fun runCommand(command: String): List<String> {
        if (process == null) {
            startProcess()
        }
        GhcModConsole.getInstance(project).append(command + "\n", GhcModConsole.MessageType.INPUT)
        val result = synchronized(process!!) {
            if (isStopped()) {
                process = null
                startProcess();
            }
            val process = process
            if (process == null) {
                listOf<String>()
            } else {
                val input = InputStreamReader(process.inputStream!!)
                val output = OutputStreamWriter(process.outputStream!!)
                output.write(command + "\n")
                output.flush()

                val lines = ArrayList<String>()

                while (lines.size < 2 ||
                (!lines[lines.size - 2].startsWith("OK") &&
                !lines[lines.size - 2].startsWith("NG"))) {
                    val char = CharArray(16 * 1024)
                    val size = input.read(char)
                    if (size == -1) {
                        break
                    }
                    val result = String(char, 0, size)
                    val split = result.split(OSUtil.newLine, limit = 0)
                    if (lines.isEmpty()) {
                        lines.add(split[0])
                    } else {
                        val last = lines.size - 1
                        lines[last] = lines[last] + split[0]
                    }
                    lines.addAll(split.toList().subList(1, split.size))
                }
                if (lines[lines.size - 2].startsWith("NG")) {
                    val hyperlinkHandler = object : NotificationListener.Adapter() {
                        override fun hyperlinkActivated(notification: Notification, e: HyperlinkEvent) {
                            notification.expire()
                            if (!project.isDisposed) {
                                ShowSettingsUtil.getInstance()?.showSettingsDialog(project, HaskellConfigurable::class.java)
                            }
                        }
                    }
                    Notifications.Bus.notify(Notification(
                            "ghc.modi",
                            "ghc-modi failed",
                            "ghc-modi failed with error: " + lines[lines.size - 2] +
                            "<br/>You can disable ghc-modi in <a href=\"#\">Settings | Haskell</a>",
                            NotificationType.ERROR, hyperlinkHandler))
                }
                lines
            }
        }
        for (line in result) {
            GhcModConsole.getInstance(project).append(line + "\n", GhcModConsole.MessageType.OUTPUT)

        }
        return result
    }

}