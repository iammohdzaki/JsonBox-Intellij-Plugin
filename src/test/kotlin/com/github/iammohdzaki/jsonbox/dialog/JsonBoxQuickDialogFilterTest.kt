package com.github.iammohdzaki.jsonbox.dialog

import com.github.iammohdzaki.jsonbox.persistance.JsonQuickListState
import com.github.iammohdzaki.jsonbox.persistance.JsonQuickListState.SortType
import com.github.iammohdzaki.jsonbox.persistance.model.JsonItem
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.GraphicsEnvironment

class JsonBoxQuickDialogFilterTest : BasePlatformTestCase() {

    private lateinit var state: JsonQuickListState

    override fun setUp() {
        super.setUp()
        state = project.service<JsonQuickListState>()
        state.items.clear()
        
        // Add some test items
        state.add(JsonItem("1", "App Config", """{"env": "prod"}""", System.currentTimeMillis() - 10000, listOf("prod", "config")))
        state.add(JsonItem("2", "User Profile", """{"name": "Zaki"}""", System.currentTimeMillis() - 5000, listOf("user", "api")))
        state.add(JsonItem("3", "Dev DB", """{"db": "localhost"}""", System.currentTimeMillis(), listOf("dev", "config")))
    }

    override fun tearDown() {
        state.items.clear()
        super.tearDown()
    }

    fun testTagFiltering() {
        if (GraphicsEnvironment.isHeadless()) return
        val dialog = JsonBoxQuickDialog(project)
        try {
            // Wait for items to load
            assertEquals(3, dialog.listModel.size)

            // Select "config" tag
            dialog.tagFilterComboBox.selectedItem = "config"
            
            assertEquals("Should filter to items with 'config' tag", 2, dialog.listModel.size)
            val titles = (0 until dialog.listModel.size).map { dialog.listModel.getElementAt(it).title }
            assertTrue(titles.contains("App Config"))
            assertTrue(titles.contains("Dev DB"))

            // Select "user" tag
            dialog.tagFilterComboBox.selectedItem = "user"
            assertEquals("Should filter to items with 'user' tag", 1, dialog.listModel.size)
            assertEquals("User Profile", dialog.listModel.getElementAt(0).title)
            
            // Select "All Tags"
            dialog.tagFilterComboBox.selectedIndex = 0
            assertEquals("Should show all items", 3, dialog.listModel.size)
        } finally {
            dialog.dispose()
        }
    }

    fun testSearchQueryWithTags() {
        if (GraphicsEnvironment.isHeadless()) return
        val dialog = JsonBoxQuickDialog(project)
        try {
            dialog.tagFilterComboBox.selectedItem = "config"
            
            // Search within the filtered tags
            dialog.searchField.text = "dev"
            
            assertEquals(1, dialog.listModel.size)
            assertEquals("Dev DB", dialog.listModel.getElementAt(0).title)
        } finally {
            dialog.dispose()
        }
    }

    fun testSearchQueryMatchesTags() {
        if (GraphicsEnvironment.isHeadless()) return
        val dialog = JsonBoxQuickDialog(project)
        try {
            // Ensure All Tags is selected
            dialog.tagFilterComboBox.selectedIndex = 0
            
            // Search for a tag name directly via text search
            dialog.searchField.text = "api"
            
            assertEquals("Text search should match tag names", 1, dialog.listModel.size)
            assertEquals("User Profile", dialog.listModel.getElementAt(0).title)
        } finally {
            dialog.dispose()
        }
    }

    fun testSortingLogic() {
        if (GraphicsEnvironment.isHeadless()) return
        
        // Setup initial sorting
        state.sortType = SortType.DATE_DESC
        
        val dialog = JsonBoxQuickDialog(project)
        try {
            // DATE_DESC: Newest first
            // Dev DB (now) > User Profile (-5s) > App Config (-10s)
            assertEquals("Dev DB", dialog.listModel.getElementAt(0).title)
            assertEquals("User Profile", dialog.listModel.getElementAt(1).title)
            assertEquals("App Config", dialog.listModel.getElementAt(2).title)

            // Change to DATE_ASC
            state.sortType = SortType.DATE_ASC
            // Since there's no public re-load method, we can trigger applyFilter by setting text
            dialog.searchField.text = " "
            dialog.searchField.text = ""
            
            assertEquals("App Config", dialog.listModel.getElementAt(0).title)
            assertEquals("User Profile", dialog.listModel.getElementAt(1).title)
            assertEquals("Dev DB", dialog.listModel.getElementAt(2).title)

            // Change to NAME_ASC
            state.sortType = SortType.NAME_ASC
            dialog.searchField.text = " "
            dialog.searchField.text = ""
            
            assertEquals("App Config", dialog.listModel.getElementAt(0).title)
            assertEquals("Dev DB", dialog.listModel.getElementAt(1).title)
            assertEquals("User Profile", dialog.listModel.getElementAt(2).title)
        } finally {
            dialog.dispose()
        }
    }
}
