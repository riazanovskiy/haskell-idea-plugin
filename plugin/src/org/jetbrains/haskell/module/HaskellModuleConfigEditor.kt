package org.jetbrains.haskell.module

import com.intellij.openapi.module.ModuleConfigurationEditor
import com.intellij.openapi.roots.ui.configuration.ContentEntriesEditor
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState

class HaskellModuleConfigEditor() : ModuleConfigurationEditorProvider {

    override fun createEditors(state: ModuleConfigurationState?): Array<ModuleConfigurationEditor> {
        val module = state!!.rootModel!!.module

        return arrayOf(ContentEntriesEditor(module.name, state),
                //PackagesEditor(state, module.getProject()),
                OutputEditor(state));
    }
}
