package org.jetbrains.haskell.external

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.jetbrains.cabal.CabalInterface
import org.jetbrains.haskell.util.LineColPosition
import org.jetbrains.haskell.util.ProcessRunner
import org.jetbrains.haskell.util.getRelativePath
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import java.io.IOException

/**
 * Created by atsky on 12/05/14.
 */
class BuildWrapper(val moduleRoot: String,
                   val cabalFile : String) {
    companion object {

        fun init(element : PsiElement) : BuildWrapper {
            val moduleRoot = BuildWrapper.getModuleContentDir(element)!!
            val virtualFile = CabalInterface.findCabal(element)!!

            return BuildWrapper(moduleRoot.path, virtualFile.path)
        }

        fun getProgramPath(): String {
            throw UnsupportedOperationException()
        }

        fun check() : Boolean {
            try {
                ProcessRunner(null).executeOrFail(getProgramPath(), "-V")
                return true
            } catch(e : IOException) {
                return false
            }

        }

        fun getModuleContentDir(file: PsiElement): VirtualFile? {
            val module = ModuleUtilCore.findModuleForPsiElement(file)
            return module?.moduleFile?.parent
        }
    }

    fun thingatpoint(file : VirtualFile, pos : LineColPosition): JSONObject? {

        val relativePath = getRelativePath(moduleRoot, file.path)

        try {
            val out = ProcessRunner(moduleRoot).executeOrFail(
                    getProgramPath(), "thingatpoint",
                    "-t", ".buildwrapper",
                    "--cabalfile=" + cabalFile,
                    "-f", relativePath,
                    "--line", pos.myLine.toString(),
                    "--column", pos.myColumn.toString())

            val array = extractJsonArray(out)
            return if (array != null) array[0] as JSONObject? else null
        } catch (e : IOException) {
            Notifications.Bus.notify(Notification("BuildWrapper.Error", "BuildWrapper error", e.message!!, NotificationType.ERROR))
            return null
        }
    }


    fun extractJsonArray(text : String) : JSONArray? {
        val prefix = "build-wrapper-json:"
        if (text.contains(prefix)) {
            val jsonText = text.substring(text.indexOf(prefix) + prefix.length)
            return JSONValue.parse(jsonText) as? JSONArray
        }

        return null
    }

    fun namesinscope(file : String): JSONArray? {
        val out = ProcessRunner(moduleRoot).executeNoFail(
                getProgramPath(), "namesinscope", "-t", ".buildwrapper", "--cabalfile=" + cabalFile, "-f", file)

        val array = extractJsonArray(out)
        return if (array != null) array[0] as JSONArray? else null
    }

    fun synchronize() {
        ProcessRunner(moduleRoot).executeNoFail(
                getProgramPath(), "synchronize", "-t", ".buildwrapper", "--cabalfile=" + cabalFile)
    }

    fun build1(file : VirtualFile) : JSONArray? {
        val relativePath = getRelativePath(moduleRoot, file.path)

        val out = ProcessRunner(moduleRoot).executeNoFail(
                getProgramPath(), "build1", "-t", ".buildwrapper", "--cabalfile=" + cabalFile, "-f", relativePath)

        val errorPrefix = "At least the following dependencies are missing:"
        if (out.contains(errorPrefix)) {
            val errorText = out.substring(out.indexOf(errorPrefix) + errorPrefix.length)
            errorText.substring(0, errorText.indexOf("\n\n"))
            Notifications.Bus.notify(Notification("Cabal.Error",
                                                  "Packages missing",
                                                  errorText.substring(0, errorText.indexOf("\n\n")),
                                                  NotificationType.WARNING))
        }

        return extractJsonArray(out)
    }

    fun dependencies() : JSONArray? {
        val out = ProcessRunner(moduleRoot).executeNoFail(
                getProgramPath(), "dependencies", "-t", ".buildwrapper", "--cabalfile=" + cabalFile)

        return extractJsonArray(out);
    }
}
