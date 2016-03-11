package org.jetbrains.haskell

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import org.jetbrains.haskell.module.HaskellModuleType
import org.jetbrains.cabal.CabalInterface
import com.intellij.util.ui.UIUtil
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.options.ShowSettingsUtil
import org.jetbrains.haskell.config.HaskellConfigurable
import com.intellij.openapi.module.Module
import java.io.File
import org.jetbrains.haskell.util.deleteRecursive
import org.jetbrains.haskell.util.OSUtil
import org.jetbrains.haskell.external.GhcMod
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.haskell.sdk.HaskellSdkType


class HaskellProjectComponent(val project: Project) : ProjectComponent {
    companion object {
        val GHC_PATH_NOT_FOUND = "ghc not found in PATH. It can cause issues."+
                                 " Please spicify haskell SDK for project."
    }

    fun invokeInUI(block: () -> Unit) {
        UIUtil.invokeAndWaitIfNeeded(Runnable { block() });
    }

    fun packageNotFound(pkg: String) {
        invokeInUI {
            val result = Messages.showDialog(
                    project,
                    pkg + " not found. You can install it by cabal or set path in settings.",
                    pkg + " executable not found",
                    arrayOf("Install", "Open settings", "Close"),
                    0,
                    null)
            if (result == 0) {
                CabalInterface(project).install(pkg)
            } else if (result == 1) {
                ShowSettingsUtil.getInstance()!!.editConfigurable(project, HaskellConfigurable());
            }
        }
    }

    fun getHaskellModules(): List<Module> {
        val moduleManager = ModuleManager.getInstance(project)!!
        return moduleManager.modules.filter { ModuleType.get(it) == HaskellModuleType.INSTANCE }
    }

    override fun projectOpened() {
        if (!getHaskellModules().isEmpty()) {
            val paths = System.getenv("PATH")!!.split(File.pathSeparator.toRegex()).toTypedArray().toMutableList()

            val sdk = ProjectRootManager.getInstance(project).projectSdk
            if (sdk != null && sdk.sdkType is HaskellSdkType) {
                paths.add(sdk.homePath + File.separator + "bin")
            }

            if (OSUtil.isMac) {
                paths.add("/usr/local/bin")
            }
            val ghcFound = paths.any {
                File(it, OSUtil.getExe("ghc")).exists()
            }
            if (!ghcFound) {

                Messages.showDialog(
                        project,
                        GHC_PATH_NOT_FOUND,
                        "ghc not found",
                        arrayOf("Close"),
                        0,
                        null)
            }



            val cabalFound = CabalInterface(project).checkVersion()
            if (!cabalFound) {
                invokeInUI {
                    val result = Messages.showDialog(
                            project,
                            "Cabal executable not found. Please add it to PATH or specify path in settings",
                            "Cabal not found",
                            arrayOf("Open settings", "Close"),
                            0,
                            null)
                    if (result == 0) {
                        ShowSettingsUtil.getInstance()!!.editConfigurable(project, HaskellConfigurable());
                    }
                }
            } else {
                if (!GhcMod.сheck()) {
                    packageNotFound("ghc-mod")
                }
            }
        }
    }


    override fun projectClosed() {
    }

    override fun getComponentName(): String {
        return "HaskellProjectComponent"
    }

    override fun initComponent() {
    }

    override fun disposeComponent() {
    }

    @Deprecated("")
    private fun removeTempDir() {
        for (module in getHaskellModules()) {
            val path = module.moduleFile?.parent?.path
            if (path != null) {
                val buildWrapperPath = File(path, ".buildwrapper")
                if (buildWrapperPath.exists()) {
                    deleteRecursive(buildWrapperPath)
                }
            }
        }
    }

}
