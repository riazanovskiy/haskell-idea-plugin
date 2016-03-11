package org.jetbrains.cabal.export

//import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataManager
//import org.jetbrains.cabal.settings.CabalProjectSettings
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataManager
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.cabal.util.SYSTEM_ID
import org.jetbrains.haskell.icons.HaskellIcons
import org.jetbrains.haskell.sdk.HaskellSdkType
import java.io.File
import javax.swing.Icon


class CabalProjectImportBuilder(dataManager: ProjectDataManager)
        : AbstractExternalProjectImportBuilder<ImportFromCabalControl>(dataManager, ImportFromCabalControl(), SYSTEM_ID) {

    override fun getName(): String = "Cabal"

    override fun getIcon(): Icon   = HaskellIcons.CABAL

//    override fun getList(): MutableList<CabalProjectSettingsControl>? {
//        return arrayList(CabalProjectSettingsControl(CabalProjectSettings()))
//    }
//
//    override fun isMarked(element: CabalProjectSettingsControl?): Boolean {
//        return false
//    }

//    throws(javaClass<ConfigurationException>())
//    override fun setList(list: List<CabalProjectSettingsControl>?) {
//    }
//
//    override fun setOpenProjectSettingsAfter(on: Boolean) {
//    }

    override fun isSuitableSdkType(sdkType: SdkTypeId?): Boolean {
        return sdkType is HaskellSdkType
    }

//    override fun commit(project: Project?, model: ModifiableModuleModel?, modulesProvider: ModulesProvider?, artifactModel: ModifiableArtifactModel?): MutableList<Module>? {
//        return null
//    }

    override fun doPrepare(context: WizardContext) {
        var pathToUse = fileToImport!!
        val file = LocalFileSystem.getInstance()!!.refreshAndFindFileByPath(pathToUse)
        if (file != null && file.isDirectory) {
            pathToUse = File(pathToUse).absolutePath
        }
        getControl(context.project).setLinkedProjectPath(pathToUse)
    }

    override fun beforeCommit(dataNode: DataNode<ProjectData>, project: Project) {
    }

    override fun applyExtraSettings(context: WizardContext) {
    }

    override fun getExternalProjectConfigToUse(file: File): File {
        return if (file.isDirectory) file else file.parentFile!!
    }
}