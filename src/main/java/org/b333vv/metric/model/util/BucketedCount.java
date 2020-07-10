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

package org.b333vv.metric.model.util;

import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

public class BucketedCount<T> {

    private final TObjectIntHashMap<T> buckets = new TObjectIntHashMap<>();

    public void createBucket(@NotNull T bucketName) {
        if (!buckets.containsKey(bucketName)) {
            buckets.put(bucketName, 0);
        }
    }

    public void incrementBucketValue(@NotNull T bucketName, int increment) {
        if (buckets.containsKey(bucketName)) {
            buckets.adjustValue(bucketName, increment);
        } else {
            buckets.put(bucketName, increment);
        }
    }

    public void incrementBucketValue(@NotNull T bucketName) {
        incrementBucketValue(bucketName, 1);
    }

    public int getBucketValue(T bucketName) {
        if (!buckets.containsKey(bucketName)) {
            return 0;
        }
        return buckets.get(bucketName);
    }

}
