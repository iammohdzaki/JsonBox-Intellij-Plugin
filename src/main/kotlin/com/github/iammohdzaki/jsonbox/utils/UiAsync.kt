package com.github.iammohdzaki.jsonbox.utils

import com.intellij.openapi.application.ApplicationManager

/**
 * Utility for executing tasks in the background and updating the UI on the Event Dispatch Thread (EDT).
 */
object UiAsync {

    /**
     * Runs a task on a pooled background thread and then executes a UI callback on the EDT with the result.
     *
     * @param T The type of the result produced by the background task.
     * @param task The background task to execute.
     * @param ui The callback to execute on the EDT with the task result.
     */
    fun <T> runBackground(
        task: () -> T,
        ui: (T) -> Unit
    ) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = task()
            ApplicationManager.getApplication().invokeLater {
                ui(result)
            }
        }
    }
}