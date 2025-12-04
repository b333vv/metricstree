/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.b333vv.metric.util.EditorUtils;
import org.jetbrains.annotations.Nullable;

public class EditorController {

    private final Project project;
    private boolean shouldMoveCaret = true;

    public EditorController(Project project) {
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

            // Access PSI element's text offset in a read action
            final int textOffset = com.intellij.openapi.application.ReadAction.compute(() -> element.getTextOffset());
            if (textOffset < editor.getDocument().getTextLength()) {
                editor.getCaretModel().moveToOffset(textOffset);
                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            }
        }
        shouldMoveCaret = true;
    }

    private boolean shouldMoveCaret(PsiElement element) {
        return shouldMoveCaret &&
                EditorUtils.isElementInSelectedFile(project, element);
    }

    @Nullable
    private Editor getEditor(PsiElement element) {
        return EditorUtils.getEditorIfSelected(project, element);
    }

    @Nullable
    public Editor openInEditor(PsiElement element) {
        // Compute PSI-dependent values in a read action
        final PsiFile psiFile;
        final int textOffset;
        final com.intellij.openapi.vfs.VirtualFile virtualFile;

        try {
            // Wrap PSI access in read action
            psiFile = com.intellij.openapi.application.ReadAction.compute(() -> {
                if (element instanceof PsiFile) {
                    return (PsiFile) element;
                } else {
                    return element.getContainingFile();
                }
            });

            if (psiFile == null) {
                return null;
            }

            virtualFile = com.intellij.openapi.application.ReadAction.compute(() -> psiFile.getVirtualFile());

            if (virtualFile == null) {
                return null;
            }

            textOffset = com.intellij.openapi.application.ReadAction.compute(() -> {
                if (element instanceof PsiFile) {
                    return -1;
                } else {
                    return element.getTextOffset();
                }
            });
        } catch (Exception e) {
            // Handle any exceptions during read action
            return null;
        }

        final OpenFileDescriptor fileDesc = new OpenFileDescriptor(project, virtualFile, textOffset);
        disableMovementOneTime();

        // Use invokeLater with proper modality state to ensure write-safe context
        ApplicationManager.getApplication().invokeLater(() -> {
            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            Editor editor = fileEditorManager.openTextEditor(fileDesc, false);

            // Move caret after opening the file
            if (editor != null && textOffset >= 0) {
                moveEditorCaret(element);
            }
        }, ModalityState.NON_MODAL);

        return null; // Cannot return editor when invoked asynchronously
    }
}
