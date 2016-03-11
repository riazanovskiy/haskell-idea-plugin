package org.jetbrains.haskell.module

import com.intellij.openapi.module.ModuleType
import org.jetbrains.haskell.fileType.HaskellFileType
import org.jetbrains.haskell.icons.HaskellIcons
import javax.swing.Icon

class HaskellModuleType() : ModuleType<HaskellModuleBuilder>("HASKELL_MODULE") {

    override fun createModuleBuilder(): HaskellModuleBuilder {
        return HaskellModuleBuilder()
    }

    override fun getName(): String {
        return "Haskell Module"
    }

    override fun getDescription(): String {
        return "Haskell Module"
    }

    override fun getBigIcon(): Icon {
        return HaskellIcons.DEFAULT
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return HaskellFileType.INSTANCE.icon
    }

    companion object {
        val INSTANCE: HaskellModuleType = HaskellModuleType()
    }
}
