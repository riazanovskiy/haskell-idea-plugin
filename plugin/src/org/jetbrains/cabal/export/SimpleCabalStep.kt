package org.jetbrains.cabal.export

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractImportFromExternalSystemWizardStep
import com.intellij.openapi.options.ConfigurationException
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class SimpleCabalStep(context: WizardContext) : AbstractImportFromExternalSystemWizardStep(context) {

    private val myComponent = JPanel(BorderLayout())

    private var mySettingsInitialised = false
    private var myControl: ImportFromCabalControl? = null

    override fun getBuilder(): CabalProjectImportBuilder? {
        return wizardContext.projectBuilder as CabalProjectImportBuilder
    }

    override fun getComponent(): JComponent {
        return myComponent
    }

    override fun getWizardContext(): WizardContext {
        return super.getWizardContext()!!
    }

    override fun updateStep() {
        if (!mySettingsInitialised) {
            initSimpleCabalControl()
        }
    }

    override fun updateDataModel() {
    }

    @Throws(ConfigurationException::class)
    override fun validate(): Boolean {
        myControl?.apply()
        if (myControl?.projectFormatPanel != null) {
            myControl!!.projectFormatPanel!!.updateData(wizardContext)
        }
        val builder = builder ?: return false
        builder.ensureProjectIsDefined(wizardContext)
        return true
    }


    private fun initSimpleCabalControl() {
        val builder = builder ?: return
        builder.prepare(wizardContext)
        myControl = builder.getControl(wizardContext.project)
        myComponent.add(myControl!!.component)
        mySettingsInitialised = true
    }
}