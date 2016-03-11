package org.jetbrains.cabal.settings

import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic


class CabalSettings(project: Project)
        : AbstractExternalSystemSettings<CabalSettings, CabalProjectSettings, CabalSettingsListener>(CabalSettingsListener.TOPIC, project) {

    override fun subscribe(listener: ExternalSystemSettingsListener<CabalProjectSettings>) {
        project.messageBus.connect(project).subscribe(CabalSettingsListener.TOPIC as Topic<ExternalSystemSettingsListener<CabalProjectSettings>>, listener)
    }

    override fun copyExtraSettingsFrom(settings: CabalSettings) {
    }

    override fun checkSettings(old: CabalProjectSettings, current: CabalProjectSettings) {
//        if (!Comparing.equal(old.getCabalHome(), current.getCabalHome())) {
//            ....
//        }
    }
}
