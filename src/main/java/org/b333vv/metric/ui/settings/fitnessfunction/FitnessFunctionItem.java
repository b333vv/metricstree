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

package org.b333vv.metric.ui.settings.fitnessfunction;

import java.util.Objects;

public class FitnessFunctionItem {
    private String name;
    private boolean isLong;
    private long minLongValue;
    private long maxLongValue;
    private double minDoubleValue;
    private double maxDoubleValue;

    public FitnessFunctionItem() {
    }

    public FitnessFunctionItem(String name, boolean isLong, long minLongValue, long maxLongValue,
                               double minDoubleValue, double maxDoubleValue) {
        this.name = name;
        this.isLong = isLong;
        this.minLongValue = minLongValue;
        this.maxLongValue = maxLongValue;
        this.minDoubleValue = minDoubleValue;
        this.maxDoubleValue = maxDoubleValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMinLongValue() {
        return minLongValue;
    }

    public void setMinLongValue(long minLongValue) {
        this.minLongValue = minLongValue;
    }

    public long getMaxLongValue() {
        return maxLongValue;
    }

    public void setMaxLongValue(long maxLongValue) {
        this.maxLongValue = maxLongValue;
    }

    public boolean isLong() {
        return isLong;
    }

    public void setLong(boolean aLong) {
        isLong = aLong;
    }

    public double getMinDoubleValue() {
        return minDoubleValue;
    }

    public void setMinDoubleValue(double minDoubleValue) {
        this.minDoubleValue = minDoubleValue;
    }

    public double getMaxDoubleValue() {
        return maxDoubleValue;
    }

    public void setMaxDoubleValue(double maxDoubleValue) {
        this.maxDoubleValue = maxDoubleValue;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FitnessFunctionItem that = (FitnessFunctionItem) o;
        return isLong == that.isLong &&
                minLongValue == that.minLongValue &&
                maxLongValue == that.maxLongValue &&
                Double.compare(that.minDoubleValue, minDoubleValue) == 0 &&
                Double.compare(that.maxDoubleValue, maxDoubleValue) == 0 &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isLong, minLongValue, maxLongValue, minDoubleValue, maxDoubleValue);
    }
}
