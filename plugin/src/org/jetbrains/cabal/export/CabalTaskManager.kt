package org.jetbrains.cabal.export

import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager

class CabalTaskManager() : ExternalSystemTaskManager<ExternalSystemExecutionSettings> {


    @Throws(ExternalSystemException::class)
    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
        return false
    }

    @Throws(ExternalSystemException::class)
    override fun executeTasks(
            id: ExternalSystemTaskId,
            taskNames: MutableList<String>,
            projectPath: String,
            settings: ExternalSystemExecutionSettings?,
            vmOptions: MutableList<String>,
            arg: MutableList<String>,
            scriptParameters: String?,
            listener: ExternalSystemTaskNotificationListener): Unit {
        return
    }
}

