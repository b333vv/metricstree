package org.jacoquev.model.metric.util;

import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;

public class CommonUtils {
    private CommonUtils() {

    }
    public static long countLines(PsiElement element) {
        if (element instanceof PsiCompiledElement) {
            return 0;
        }
        final String text = element.getText();
        element.getContainingFile().getVirtualFile().getDetectedLineSeparator();
        return countLines(text);
    }

    static long countLines(String text) {
        long lines = 0;
        boolean onEmptyLine = true;
        final char[] chars = text.toCharArray();
        for (final char aChar : chars) {
            if (aChar == '\n' || aChar == '\r') {
                if (!onEmptyLine) {
                    lines++;
                    onEmptyLine = true;
                }
            } else if (aChar == ' ' || aChar == '\t') {
            } else {
                onEmptyLine = false;
            }
        }
        if (!onEmptyLine) {
            lines++;
        }
        return lines;
    }
}
