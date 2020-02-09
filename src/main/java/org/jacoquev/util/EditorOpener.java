package org.jacoquev.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class EditorOpener {

    private final Project project;
    private boolean shouldMoveCaret = true;

    public EditorOpener(Project project) {
        this.project = project;
    }

    private void disableMovementOneTime() {
        shouldMoveCaret = false;
    }

    public void moveEditorCaret(PsiElement element) {
        if (element == null) {
            return;
        }
        if (shouldMoveCaret(element)) {
            final Editor editor = getEditor(element);
            if (editor == null) {
                return;
            }

            final int textOffset = element.getTextOffset();
            if (textOffset < editor.getDocument().getTextLength()) {
                editor.getCaretModel().moveToOffset(textOffset);
                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            }
        }
        shouldMoveCaret = true;
    }

    private boolean shouldMoveCaret(PsiElement element) {
        return shouldMoveCaret &&
                MetricsUtils.isElementInSelectedFile(project, element);
    }

    @Nullable
    private Editor getEditor(PsiElement element) {
        return MetricsUtils.getEditorIfSelected(project, element);
    }

    @Nullable
    public Editor openInEditor(PsiElement element) {

        final PsiFile psiFile;
        final int i;
        if (element instanceof PsiFile) {
            psiFile = (PsiFile) element;
            i = -1;
        } else {
            psiFile = element.getContainingFile();
            i = element.getTextOffset();
        }
        if (psiFile == null) {
            return null;
        }
        final OpenFileDescriptor fileDesc =
                new OpenFileDescriptor(project, psiFile.getVirtualFile(), i);
        disableMovementOneTime();
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        return fileEditorManager.openTextEditor(fileDesc, false);
    }
}
