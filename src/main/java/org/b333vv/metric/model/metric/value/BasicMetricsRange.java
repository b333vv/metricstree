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

import static org.b333vv.metric.model.metric.value.RangeType.*;

public class BasicMetricsRange implements Range {
    private final Value regular;
    private final Value high;
    private final Value veryHigh;

    public static final BasicMetricsRange UNDEFINED = new BasicMetricsRange(Value.UNDEFINED, Value.UNDEFINED, Value.UNDEFINED) {
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

    private BasicMetricsRange(@NotNull Value regular, @NotNull Value high, @NotNull Value veryHigh) {
        this.regular = regular;
        this.high = high;
        this.veryHigh = veryHigh;
    }

    public static BasicMetricsRange of(@NotNull Value regular, @NotNull Value high, @NotNull Value veryHigh) {
        if (regular == Value.UNDEFINED || high == Value.UNDEFINED || veryHigh == Value.UNDEFINED) {
            return UNDEFINED;
        }
        if (regular.isGreaterThan(high) || high.isGreaterThan(veryHigh) || regular.isGreaterThan(veryHigh)) {
            return UNDEFINED;
        }
        return new BasicMetricsRange(regular,high, veryHigh);
    }

    @Override
    public RangeType getRangeType(@NotNull final Value value) {
        if (value == Value.UNDEFINED) {
            return RangeType.UNDEFINED;
        }
        if (value.isEqualsOrGreaterThan(veryHigh)) {
            return EXTREME;
        }
        if (value.isEqualsOrGreaterThan(high) && value.isLessThan(veryHigh)) {
            return VERY_HIGH;
        }
        if (value.isEqualsOrGreaterThan(regular) && value.isLessThan(high)) {
            return HIGH;
        }
        if (value.isEqualsOrGreaterThan(Value.ZERO) && value.isLessThan(regular)) {
            return REGULAR;
        }
        return RangeType.UNDEFINED;
    }

    @Override
    public Value getRegularFrom() {
        return Value.ZERO;
    }

    @Override
    public Value getRegularTo() {
        return regular;
    }

    @Override
    public Value getHighFrom() {
        return regular;
    }

    @Override
    public Value getHighTo() {
        return high;
    }

    @Override
    public Value getVeryHighFrom() {
        return high;
    }

    @Override
    public Value getVeryHighTo() {
        return veryHigh;
    }

    @Override
    public Value getExtremeFrom() {
        return veryHigh;
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
        switch (rangeType) {
            case REGULAR:
                return "[" + getRegularFrom() + ".." + getRegularTo() + ")";
            case HIGH:
                return "[" + getHighFrom() + ".." + getHighTo() + ")";
            case VERY_HIGH:
                return "[" + getVeryHighFrom() + ".." + getVeryHighTo() + ")";
            case EXTREME:
                return "[" + getExtremeFrom() + "..\u221E)";
//                return "[" + getExtremeFrom() + ".." + getExtremeTo() + ")";
            default:
                return "";
        }
    }

    @Override
    public String percentageFormat() {
        return "[" + getRegularFrom().percentageFormat() + ".." + getRegularTo().percentageFormat() + ")";
    }
}
