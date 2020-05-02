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

package org.b333vv.metric.ui.settings.ranges;

import org.b333vv.metric.model.metric.MetricType;

import java.util.Objects;

public class MetricsValidRangeStub {
//    private MetricType type;
    private String name;
    private String description;
    private String level;
    private boolean doubleValue;
    private double minDoubleValue;
    private double maxDoubleValue;
    private long minLongValue;
    private long maxLongValue;

    public MetricsValidRangeStub(String name, String description, String level,
                                 boolean doubleValue, double minDoubleValue, double maxDoubleValue,
                                 long minLongValue, long maxLongValue) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.doubleValue = doubleValue;
        this.minDoubleValue = minDoubleValue;
        this.maxDoubleValue = maxDoubleValue;
        this.minLongValue = minLongValue;
        this.maxLongValue = maxLongValue;
    }

    public MetricsValidRangeStub(MetricType type, boolean doubleValue,
                                 double minDoubleValue, double maxDoubleValue, long minLongValue, long maxLongValue) {
//        this.type = type;
        this.name = type.name();
        this.description = type.description();
        this.level = type.level().level();
        this.doubleValue = doubleValue;
        this.minDoubleValue = minDoubleValue;
        this.maxDoubleValue = maxDoubleValue;
        this.minLongValue = minLongValue;
        this.maxLongValue = maxLongValue;
    }

    public MetricsValidRangeStub(MetricType type, long minLongValue, long maxLongValue) {
//        this.type = type;
        this.name = type.name();
        this.description = type.description();
        this.level = type.level().level();
        this.doubleValue = false;
        this.minDoubleValue = 0.0;
        this.maxDoubleValue = 0.0;
        this.minLongValue = minLongValue;
        this.maxLongValue = maxLongValue;
    }

    public MetricsValidRangeStub(MetricType type, double minDoubleValue, double maxDoubleValue) {
//        this.type = type;
        this.name = type.name();
        this.description = type.description();
        this.level = type.level().level();
        this.doubleValue = true;
        this.minDoubleValue = minDoubleValue;
        this.maxDoubleValue = maxDoubleValue;
        this.minLongValue = 0;
        this.maxLongValue = 0;
    }

    public MetricsValidRangeStub(){}

//    public MetricType getType() {
//        return type;
//    }
//
//    public void setType(MetricType type) {
//        this.type = type;
//    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setDoubleValue(boolean doubleValue) {
        this.doubleValue = doubleValue;
    }

    public boolean isDoubleValue() {
        return doubleValue;
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


    public MetricType getType() {
        return MetricType.valueOf(name);
    }


    @Override
    public String toString() {
//        return "[" + type.name() + "] " + type.description();
        return "[" + name + "] " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetricsValidRangeStub)) return false;
        MetricsValidRangeStub that = (MetricsValidRangeStub) o;
        return isDoubleValue() == that.isDoubleValue() &&
                Double.compare(that.getMinDoubleValue(), getMinDoubleValue()) == 0 &&
                Double.compare(that.getMaxDoubleValue(), getMaxDoubleValue()) == 0 &&
                getMinLongValue() == that.getMinLongValue() &&
                getMaxLongValue() == that.getMaxLongValue() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getLevel(), that.getLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getLevel(), isDoubleValue(), getMinDoubleValue(), getMaxDoubleValue(), getMinLongValue(), getMaxLongValue());
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof MetricsValidRangeStub)) return false;
//        MetricsValidRangeStub that = (MetricsValidRangeStub) o;
//        return isDoubleValue() == that.isDoubleValue() &&
//                Double.compare(that.getMinDoubleValue(), getMinDoubleValue()) == 0 &&
//                Double.compare(that.getMaxDoubleValue(), getMaxDoubleValue()) == 0 &&
//                getMinLongValue() == that.getMinLongValue() &&
//                getMaxLongValue() == that.getMaxLongValue() &&
//                getType() == that.getType();
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getType(), isDoubleValue(), getMinDoubleValue(),
//                getMaxDoubleValue(), getMinLongValue(), getMaxLongValue());
//    }
}
