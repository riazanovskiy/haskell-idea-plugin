package org.jetbrains.haskell.module

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.haskell.icons.HaskellIcons
import org.jetbrains.haskell.sdk.HaskellSdkType
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.swing.Icon

class HaskellModuleBuilder() : ModuleBuilder() {


    override fun getBuilderId() = "haskell.module.builder"

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? =
        StdModuleTypes.JAVA!!.modifySettingsStep(settingsStep, this)

    override fun getBigIcon(): Icon = HaskellIcons.HASKELL_BIG

    override fun getGroupName(): String? = "Haskell"

    override fun getPresentableName(): String? = "Haskell"

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep> =
        moduleType.createWizardSteps(wizardContext, this, modulesProvider)

    override fun getModuleType(): HaskellModuleType {
        return HaskellModuleType.INSTANCE
    }

    override fun setupRootModel(rootModel: ModifiableRootModel?) {
        if (myJdk != null) {
            rootModel!!.sdk = myJdk
        } else {
            rootModel!!.inheritSdk()
        }

        val contentEntry = doAddContentEntry(rootModel)
        if (contentEntry != null) {
            val srcPath = contentEntryPath!! + File.separator + "src"
            File(srcPath).mkdirs()
            val sourceRoot = LocalFileSystem.getInstance()!!.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(srcPath))
            if (sourceRoot != null) {
                contentEntry.addSourceFolder(sourceRoot, false, "")
            }

            val hasCabal = File(contentEntryPath!!).list()!!.any { it.endsWith(".cabal") }
            if (!hasCabal) {
                val name = name
                try {
                    makeCabal(contentEntryPath!! + File.separator + name + ".cabal", name!!)
                    makeMain(srcPath + File.separator + "Main.hs")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun makeCabal(path: String, name: String) {
        val text = "name:              $name\nversion:           1.0\nBuild-Type:        Simple\ncabal-version:     >= 1.2\n\nexecutable $name\n  main-is:         Main.hs\n  hs-source-dirs:  src\n  build-depends:   base\n"
        val writer = FileWriter(path)
        writer.write(text)
        writer.close()
    }

    fun makeMain(path: String) {
        val text = "module Main where\n" + "\n"

        val writer = FileWriter(path)
        writer.write(text)
        writer.close()
    }

    override fun isSuitableSdkType(sdkType: SdkTypeId?): Boolean {
        return sdkType is HaskellSdkType
    }
}
