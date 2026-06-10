package com.github.iammohdzaki.jsonbox.icons

import com.intellij.openapi.util.IconLoader

/**
 * Central registry for all JsonBox icons.
 * Icons are loaded lazily via [IconLoader.getIcon] using classpath-relative paths.
 * IntelliJ automatically resolves the `_dark` variant for dark UI themes.
 */
object JsonBoxIcons {

    /**
     * 16×16 icon used in the main IDE toolbar.
     * Light theme: /icons/jsonboxToolbar.svg
     * Dark theme:  /icons/jsonboxToolbar_dark.svg  (resolved automatically)
     */
    @JvmField
    val ToolbarIcon = IconLoader.getIcon("/icons/jsonboxToolbar.svg", JsonBoxIcons::class.java)
}
