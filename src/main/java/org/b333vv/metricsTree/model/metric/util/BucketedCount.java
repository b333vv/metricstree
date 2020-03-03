package org.b333vv.metricsTree.model.metric.util;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BucketedCount<T> {

    private TObjectIntHashMap<T> buckets = new TObjectIntHashMap<T>();

    public void createBucket(@NotNull T bucketName) {
        if (!buckets.containsKey(bucketName)) {
            buckets.put(bucketName, 0);
        }
    }

    public Set<T> getBuckets() {
        Set<T> result = new HashSet<T>(buckets.size());
        buckets.forEachKey(new TObjectProcedure<T>() {
            @Override
            public boolean execute(T t) {
                result.add(t);
                return true;
            }
        });
        return result;
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

    public void clear() {
        buckets.clear();
    }
}
