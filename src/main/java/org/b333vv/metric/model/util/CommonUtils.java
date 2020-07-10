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

package org.b333vv.metric.model.util;

import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class CommonUtils {

    private CommonUtils() {
        // Utility class
    }

    public static long countLines(PsiElement element) {
        if (element instanceof PsiCompiledElement) {
            return 0;
        }
        final String text = element.getText();
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

    /**
     * Checks that two sets has non empty intersection.
     * @return {@code True} if sets have non empty intersect. (@code False) if sets have empty intersect
     * or at least one of them is null.
     */
    @Contract("null, _ -> false; _, null -> false")
    public static <T> boolean haveIntersection(final Set<T> a, final Set<T> b) {
        if (a == null || b == null) {
            return false;
        }
        final Set<T> intersection  = new HashSet<T>(a);
        intersection.retainAll(b);
        return !intersection.isEmpty();
    }

    /**
     * Calculate size of the intersection of two sets.
     * @return Size of the intersection of two sets. If one of sets is null, intersection will be considered equal to 0.
     */
    public static <T> int sizeOfIntersection(@Nullable final Set<T> a, @Nullable final Set<T> b) {
        if (a == null || b == null) {
            return 0;
        }
        final Set<T> intersection  = new HashSet<T>(a);
        intersection.retainAll(b);
        return intersection.size();
    }

    /**
     * Calculates Jaccard index for two sets(see <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard index</a>)
     * @param a First set
     * @param b Second set
     * @param <T>
     * @return Returns Jaccard index of sets
     */
    public static <T> double jaccardIndex(@Nullable final Set<T> a, @Nullable final Set<T> b) {
        if (a == null || b == null || a.size() + b.size() == 0) {
            return 1.0;
        }
        final int intersection = sizeOfIntersection(a, b);
        return (double) intersection / (double) (a.size() + b.size() - intersection);
    }

    /**
     * Calculates Jaccard distance for two sets(see <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard index</a>).
     * It is equal {@code 1 - jaccardIndex(a, b)}
     * @param a First set
     * @param b Second set
     * @param <T>
     * @return Returns Jaccard distance of sets
     */
    public static <T> double jaccardDistance(@Nullable final Set<T> a, @Nullable final Set<T> b) {
        return 1.0 - jaccardIndex(a, b);
    }
}
