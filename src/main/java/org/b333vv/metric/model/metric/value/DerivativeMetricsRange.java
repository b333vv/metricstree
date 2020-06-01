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

import static org.b333vv.metric.model.metric.value.RangeType.REGULAR;
import static org.b333vv.metric.model.metric.value.RangeType.EXTREME;

public class DerivativeMetricsRange implements Range {
    private final Value from;
    private final Value to;

    public static final DerivativeMetricsRange UNDEFINED = new DerivativeMetricsRange(Value.UNDEFINED, Value.UNDEFINED) {
        @Override
        public RangeType getRangeType(@NotNull Value value) {
            return RangeType.UNDEFINED;
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

    private DerivativeMetricsRange(@NotNull Value from, @NotNull Value to) {
        this.from = from;
        this.to = to;
    }

    public static DerivativeMetricsRange of(@NotNull Value from, @NotNull Value to) {
        if (from == Value.UNDEFINED || to == Value.UNDEFINED) {
            return UNDEFINED;
        }
        if (from.isGreaterThan(to)) {
            return UNDEFINED;
        }
        return new DerivativeMetricsRange(from, to);
    }

    @Override
    public RangeType getRangeType(@NotNull final Value value) {
        if (value.isEqualsOrGreaterThan(to) || value.isLessThan(from)) {
            return EXTREME;
        }
        if (value.isEqualsOrGreaterThan(from) && value.isLessThan(to)) {
            return REGULAR;
        }
        return RangeType.UNDEFINED;
    }

    @Override
    public Value getRegularFrom() {
        return from;
    }

    @Override
    public Value getRegularTo() {
        return to;
    }

    @Override
    public Value getHighFrom() {
        return Value.UNDEFINED;
    }

    @Override
    public Value getHighTo() {
        return Value.UNDEFINED;
    }

    @Override
    public Value getVeryHighFrom() {
        return Value.UNDEFINED;
    }

    @Override
    public Value getVeryHighTo() {
        return Value.UNDEFINED;
    }

    @Override
    public Value getExtremeFrom() {
        return to;
    }

    @Override
    public Value getExtremeTo() {
        return Value.UNDEFINED;
    }

    @Override
    public String toString() {
        return "[" + getRegularFrom() + ".." + getRegularTo() + ")";
    }

    @Override
    public String getRangeByRangeType(RangeType rangeType) {
        if (rangeType == REGULAR) {
            return "[" + getRegularFrom() + ".." + getRegularTo() + ")";
        }
        return "";
    }

    @Override
    public String percentageFormat() {
        return "[" + getRegularFrom().percentageFormat() + ".." + getRegularTo().percentageFormat() + ")";
    }
}
