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

package org.b333vv.metric.model.metric;

public enum MetricSet {
    UNDEFINED("", MetricLevel.UNDEFINED),
    HALSTEAD_METHOD("Halstead Metric Set", MetricLevel.METHOD),
    HALSTEAD_CLASS("Halstead Metric Set", MetricLevel.CLASS),
    HALSTEAD_PACKAGE("Halstead Metric Set", MetricLevel.PACKAGE),
    HALSTEAD_PROJECT("Halstead Metric Set", MetricLevel.PROJECT),
    CHIDAMBER_KEMERER("Chidamber-Kemerer Metrics Set", MetricLevel.CLASS),
    LORENZ_KIDD("Lorenz-Kidd Metrics Set", MetricLevel.CLASS),
    LI_HENRY("Li-Henry Metrics Set", MetricLevel.CLASS),
    LANZA_MARINESCU("Lanza-Marinescu Metrics Set", MetricLevel.CLASS),
    BIEMAN_KANG("Bieman-Kang Metrics Set", MetricLevel.CLASS),
    CLEMENS_LEE("Chr. Clemens Lee Metrics Set", MetricLevel.CLASS),
    R_MARTIN("Robert C. Martin Metrics Set", MetricLevel.PACKAGE),
    MOOD("MOOD Metrics Set", MetricLevel.PROJECT),
    STATISTIC("Statistics", MetricLevel.PROJECT_PACKAGE),
    QMOOD("QMOOD Quality Attributes Set", MetricLevel.PROJECT),
    MAINTAINABILITY_INDEX("Maintainability Index", MetricLevel.PROJECT);

    private final String set;
    private final MetricLevel level;
    private final String url;

    MetricSet(String set, MetricLevel level) {
        this.set = set;
        this.level = level;
        this.url = "/html/" + name() + ".html";
    }

    public String set() {
        return set;
    }
    public MetricLevel level() {
        return level;
    }
    public String url() {
        return url;
    }
}
