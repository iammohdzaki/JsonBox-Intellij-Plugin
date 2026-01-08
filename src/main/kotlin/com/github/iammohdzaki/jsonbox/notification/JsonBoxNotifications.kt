package com.github.iammohdzaki.jsonbox.notification

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * Utility for showing notifications from the JsonBox plugin.
 */
object JsonBoxNotifications {

    private const val GROUP_ID = "JsonBox Notifications"

    private val GROUP: NotificationGroup
        get() = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)

    /**
     * Shows an information notification to the user.
     *
     * @param project The current project.
     * @param title The title of the notification.
     * @param content The content message of the notification.
     */
    fun notify(project: Project, title: String, content: String) {
        GROUP
            .createNotification(title, content, NotificationType.INFORMATION)
            .notify(project)
    }
}