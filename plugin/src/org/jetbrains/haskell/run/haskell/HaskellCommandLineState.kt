package org.jetbrains.haskell.run.haskell

import com.intellij.execution.*
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.openapi.vfs.LocalFileSystem
import com.pty4j.PtyProcess
import org.jetbrains.cabal.CabalFile
import org.jetbrains.cabal.CabalInterface
import org.jetbrains.cabal.psi.Executable
import org.jetbrains.cabal.psi.FullVersionConstraint
import org.jetbrains.haskell.config.HaskellSettings
import org.jetbrains.haskell.debugger.config.DebuggerType
import org.jetbrains.haskell.debugger.config.HaskellDebugSettings
import org.jetbrains.haskell.debugger.procdebuggers.utils.RemoteDebugStreamHandler
import org.jetbrains.haskell.debugger.prochandlers.GHCiProcessHandler
import org.jetbrains.haskell.debugger.prochandlers.HaskellDebugProcessHandler
import org.jetbrains.haskell.debugger.prochandlers.RemoteProcessHandler
import org.jetbrains.haskell.debugger.repl.DebugConsoleFactory
import org.jetbrains.haskell.util.GHCUtil
import org.jetbrains.haskell.util.OSUtil
import org.jetbrains.haskell.util.joinPath
import java.io.File
import java.util.*

class HaskellCommandLineState(environment: ExecutionEnvironment, val configuration: CabalRunConfiguration) : CommandLineState(environment) {


    override fun startProcess(): ProcessHandler {
        val generalCommandLine = createCommandLine()
        val processHandler: ProcessHandler =
                if (HaskellSettings.getInstance().state.usePtyProcess!!) {
                    OSProcessHandler(getPtyProcess(generalCommandLine)!!, generalCommandLine.commandLineString,
                            CharsetToolkit.UTF8_CHARSET)
                } else {
                    OSProcessHandler(generalCommandLine)
                }
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    private fun getPtyProcess(generalCommandLine: GeneralCommandLine): PtyProcess? {
        val exePath = generalCommandLine.exePath
        val params = generalCommandLine.parametersList.list
        val env = generalCommandLine.environment
        val dir = generalCommandLine.workDirectory
        val command = Array(1 + params.size, {
            i: Int ->
            if (i == 0) exePath else params[i - 1]
        })
        return PtyProcess.exec(command, env, dir?.absolutePath, true)
    }

    private fun getDependencies(): List<FullVersionConstraint> {
        val module = configuration.module ?: throw ExecutionException("Error while starting debug process: module not specified")
        val exec = getExecutable(module)
        return exec.getBuildDepends()
    }

    /**
     * Returns pair of main file path and path to the sources directory
     */
    private fun getPaths(): Pair<String, String> {
        val module = configuration.module ?: throw ExecutionException("Error while starting debug process: module not specified")
        val exec = getExecutable(module)
        val mainFileName: String? = exec.getMainFile()?.text ?: throw ExecutionException("Error while starting debug process: no main file specified in executable section")
        val srcDirPath = tryGetSrcDirFullPath(mainFileName!!, module, exec) ?: throw ExecutionException("Error while starting debug process: main file $mainFileName not found in source directories")
        val filePath = joinPath(srcDirPath, mainFileName)
        return Pair(filePath, srcDirPath)
    }

    private fun getExecutable(module: Module): Executable {
        val cabalFile = tryGetCabalFile(module) ?: throw ExecutionException("Error while starting debug process: cabal file not found")
        val exec = tryGetExecWithNameFromConfig(cabalFile) ?: throw ExecutionException("Error while starting debug process: cabal file does not contain executable ${configuration.myExecutableName}")
        return exec
    }

    private fun startGHCiDebugProcess(): HaskellDebugProcessHandler {
        val module = configuration.module ?: throw ExecutionException("Error while starting debug process: module not specified")
        val paths = getPaths()
        val filePath = paths.first
        val srcDirPath = paths.second
        val ghciPath = GHCUtil.getCommandPath(ModuleRootManager.getInstance(module)!!.sdk!!.homeDirectory, "ghci") ?: throw ExecutionException("Error while starting debug process: ghci path not specified")

        val command: ArrayList<String> = arrayListOf(ghciPath, filePath, "-i$srcDirPath")
        val depends = getDependencies()

        for (dep in depends) {
            command.add("-package")
            command.add(dep.getBaseName())
        }
        val process = Runtime.getRuntime().exec(command.toTypedArray())
        return GHCiProcessHandler(process)
    }

    private fun startRemoteDebugProcess(): HaskellDebugProcessHandler {
        val module = configuration.module ?: throw ExecutionException("Error while starting debug process: module not specified")
        val paths = getPaths()
        val filePath = paths.first
        val srcDirPath = paths.second

        val streamHandler = RemoteDebugStreamHandler()
        streamHandler.start()

        val baseDir = module.moduleFile!!.parent!!.canonicalPath!!
        val debuggerPath = HaskellDebugSettings.getInstance().state.remoteDebuggerPath ?: throw ExecutionException("Cannot run remote debugger: path not specified")

        val command: ArrayList<String> = arrayListOf(debuggerPath, "-m$filePath", "-p${streamHandler.getPort()}", "-i$srcDirPath")
        val depends = getDependencies()
        for (dep in depends) {
            command.add("-pkg${dep.getBaseName()}")
        }
        val builder = ProcessBuilder(command).directory(File(baseDir))

        try {
            return RemoteProcessHandler(builder.start(), streamHandler)
        } catch (ex: Exception) {
            throw ExecutionException("Cannot run remote debugger in path " + debuggerPath)
        }
    }

    fun executeDebug(project: Project, executor: Executor, runner: ProgramRunner<out RunnerSettings>): ExecutionResult {
        val debuggerType = HaskellDebugSettings.getInstance().state.debuggerType
        val processHandler = if (debuggerType == DebuggerType.GHCI) {
            startGHCiDebugProcess()
        } else {
            startRemoteDebugProcess()
        }
        val console = DebugConsoleFactory.createDebugConsole(project, processHandler)
        console.attachToProcess(processHandler)

        return DefaultExecutionResult(console, processHandler)
    }

    private fun createCommandLine(): GeneralCommandLine {
        val module = configuration.module ?: throw ExecutionException("Module not specified")

        val name = configuration.myExecutableName!!
        val commandLine = GeneralCommandLine()

        val baseDir = module.moduleFile!!.parent!!.canonicalPath

        val exePath = joinPath(baseDir!!, "dist", "build", name, OSUtil.getExe(name))

        if (!File(exePath).exists()) {
            throw CantRunException("Cannot run: " + exePath)
        }

        commandLine.withWorkDirectory(configuration.workingDirectory)
        commandLine.exePath = exePath
        val parameters = configuration.programParameters
        if (parameters != null) {
            commandLine.parametersList.addParametersString(parameters)
        }
        return commandLine
    }

    private fun tryGetCabalFile(module: Module): CabalFile? {
        val project = configuration.project
        val cabalVirtualFile = CabalInterface.findCabal(module) ?: return null
        val cabalInterface = CabalInterface(project)
        return cabalInterface.getPsiFile(cabalVirtualFile)
    }

    private fun tryGetExecWithNameFromConfig(cabalFile: CabalFile): Executable? {
        val execs = cabalFile.getExecutables()
        val neededExecName = configuration.myExecutableName
        for (exec in execs) {
            if (exec.getExecutableName() == neededExecName) {
                return exec
            }
        }
        return null
    }

    private fun tryGetSrcDirFullPath(mainFileName: String, module: Module, correspondingExec: Executable): String? {
        val baseDirPath = module.moduleFile!!.parent!!.canonicalPath!!
        val srcDirs: List<String> = correspondingExec.getHsSourceDirs().map { it.text }
        for (srcDir in srcDirs) {
            val path = joinPath(baseDirPath, srcDir, mainFileName)
            val vFile = LocalFileSystem.getInstance()!!.findFileByIoFile(File(path))
            if (vFile != null && vFile.exists()) {
                return joinPath(baseDirPath, srcDir)
            }
        }
        return null
    }

}
