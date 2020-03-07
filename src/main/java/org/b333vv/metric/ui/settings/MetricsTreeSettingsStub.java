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

package org.b333vv.metric.ui.settings;

import java.util.Objects;

public class MetricsTreeSettingsStub {
    private String name;
    private String description;
    private String level;
    private String set;
    private boolean needToConsider;

    public MetricsTreeSettingsStub(String name, String description, String level,
                                   String set, boolean needToConsider) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.set = set;
        this.needToConsider = needToConsider;
    }

    public MetricsTreeSettingsStub() {
    }

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

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public boolean isNeedToConsider() {
        return needToConsider;
    }

    public void setNeedToConsider(boolean needToConsider) {
        this.needToConsider = needToConsider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetricsTreeSettingsStub)) return false;
        MetricsTreeSettingsStub that = (MetricsTreeSettingsStub) o;
        return isNeedToConsider() == that.isNeedToConsider() &&
                getName().equals(that.getName()) &&
                getDescription().equals(that.getDescription()) &&
                getLevel().equals(that.getLevel()) &&
                getSet().equals(that.getSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getLevel(), getSet(), isNeedToConsider());
    }
}
