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

package org.b333vv.metric.model.metric.util;

import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;

public class CommonUtils {
    private CommonUtils() {
//        Util class
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
