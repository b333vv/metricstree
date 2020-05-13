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

package org.b333vv.metric.model.metric.value;

import org.jetbrains.annotations.NotNull;

public class Range {
    public static final Range UNDEFINED = new Range(Value.UNDEFINED, Value.UNDEFINED) {
        @Override
        public boolean includes(Value value) {
            return true;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public String percentageFormat() {
            return "";
        }
    };

    private final Value from;
    private final Value to;

    private Range(Value from, Value to) {
        this.from = from;
        this.to = to;
    }

    public static Range of(@NotNull Value from, @NotNull Value to) {
        if (from.isGreaterThan(to)) {
            throw new IllegalArgumentException("Wrong range bounds: from > to");
        }
        return new Range(from, to);
    }

    public Value getFrom() {
        return from;
    }

    public Value getTo() {
        return to;
    }

    public boolean includes(@NotNull Value value) {
        return (value.isEqualsOrGreaterThan(from) && value.isEqualsOrLessThan(to));
    }

    @Override
    public String toString() {
        return "[" +
                from +
                ".." +
                to +
                "]";
    }

    public String percentageFormat() {
        return "[" +
                from.percentageFormat() +
                ".." +
                to.percentageFormat() +
                "]";
    }
}
