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
    UNDEFINED(""),
    CHIDAMBER_KEMERER("Chidamber-Kemerer Metrics Set"),
    LORENZ_KIDD("Lorenz-Kidd Metrics Set"),
    LI_HENRY("Li-Henry Metrics Set"),
    LANZA_MARINESCU("Lanza-Marinescu Metrics Set"),
    BIEMAN_KANG("Bieman-Kang Metrics Set"),
    CLEMENS_LEE("Chr. Clemens Lee Metrics Set"),
    R_MARTIN("Robert C. Martin Metrics Set"),
    MOOD("MOOD Metrics Set"),
    STATISTIC("Project Statistic");

    private final String set;

    MetricSet(String set) {
        this.set = set;
    }

    public String set() {
        return set;
    }
}
