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

import java.util.Objects;

public class BasicMetricsValidRangeStub {
    private String name;
    private String description;
    private String level;
    private long regularBound;
    private long highBound;
    private long veryHighBound;

    public BasicMetricsValidRangeStub(String name, String description, String level,
                                      long regularBound, long highBound, long veryHighBound) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.regularBound = regularBound;
        this.highBound = highBound;
        this.veryHighBound = veryHighBound;
    }

    public BasicMetricsValidRangeStub(){}

    public long getRegularBound() {
        return regularBound;
    }

    public void setRegularBound(long regularBound) {
        this.regularBound = regularBound;
    }

    public long getHighBound() {
        return highBound;
    }

    public void setHighBound(long highBound) {
        this.highBound = highBound;
    }

    public long getVeryHighBound() {
        return veryHighBound;
    }

    public void setVeryHighBound(long veryHighBound) {
        this.veryHighBound = veryHighBound;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicMetricsValidRangeStub)) return false;
        BasicMetricsValidRangeStub that = (BasicMetricsValidRangeStub) o;
        return getRegularBound() == that.getRegularBound() &&
                getHighBound() == that.getHighBound() &&
                getVeryHighBound() == that.getVeryHighBound() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getLevel(), that.getLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getLevel(), getRegularBound(), getHighBound(), getVeryHighBound());
    }

    @Override
    public String toString() {
        return "[" + name + "] " + description;
    }
}
