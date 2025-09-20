package com.github.iammohdzaki.jsonbox.editor

import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile

/**
 * Factory for creating PSI-backed JSON editors for JsonBox plugin.
 *
 * Supports:
 * - Syntax highlighting
 * - Code folding
 * - Line numbers
 * - Theme-aware colors
 * - File-backed or in-memory editors
 */
object JsonEditorFactory {

    /**
     * Creates an EditorEx for given file or scratch text.
     * Supports syntax highlighting, line numbers, folding.
     */
    fun createEditor(project: Project, virtualFile: VirtualFile?, initialText: String = ""): EditorEx {
        val lightFile = LightVirtualFile("temp.json", JsonLanguage.INSTANCE, initialText)
        val psiFile = PsiManager.getInstance(project).findFile(lightFile)!!
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!

        val editor = EditorFactory.getInstance().createEditor(
            document,
            project,
            FileTypeManager.getInstance().getFileTypeByExtension("json"),
            false
        ) as EditorEx

        val scheme = EditorColorsManager.getInstance().globalScheme
        editor.colorsScheme = scheme
        editor.backgroundColor = scheme.defaultBackground
        editor.settings.isLineNumbersShown = true
        editor.settings.isFoldingOutlineShown = true

        val syntaxHighlighter =
            SyntaxHighlighterFactory.getSyntaxHighlighter(JsonLanguage.INSTANCE, project, virtualFile)
        editor.highlighter = com.intellij.openapi.editor.ex.util.LexerEditorHighlighter(syntaxHighlighter, scheme)

        return editor
    }
}