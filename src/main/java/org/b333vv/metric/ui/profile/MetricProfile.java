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

package org.b333vv.metric.ui.profile;

import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MetricProfile implements Comparable<MetricProfile> {
    private final String name;
    private final Map<MetricType, Range> profile;

    public MetricProfile(String name, Map<MetricType, Range> profile) {
        this.name = name;
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public Map<MetricType, Range> getProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull MetricProfile o) {
        return name.compareTo(o.name);
    }
}
