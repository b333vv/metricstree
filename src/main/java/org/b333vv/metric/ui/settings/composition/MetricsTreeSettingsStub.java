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

package org.b333vv.metric.ui.settings.composition;

import org.b333vv.metric.model.metric.MetricType;

import java.util.Objects;

public class MetricsTreeSettingsStub {
    private MetricType type;
    private boolean needToConsider;

    public MetricsTreeSettingsStub(MetricType type, boolean needToConsider) {
        this.type = type;
        this.needToConsider = needToConsider;
    }

    public MetricsTreeSettingsStub() {
    }

    public MetricType getType() {
        return type;
    }

    public void setType(MetricType type) {
        this.type = type;
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
                getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), isNeedToConsider());
    }
}
