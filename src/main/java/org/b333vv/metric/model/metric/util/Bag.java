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

package org.b333vv.metric.model.metric.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Bag<T> {
    private final Map<T, Integer> contents = new HashMap<>();

    public void add(T obj) {
        contents.merge(obj, 1, Integer::sum);
    }

    public Set<T> getContents() {
        return contents.keySet();
    }

    public int getCountForObject(T obj) {
        return Objects.requireNonNullElse(contents.get(obj), 0);
    }
}
