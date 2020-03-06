package org.b333vv.metric.model.metric.util;

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
