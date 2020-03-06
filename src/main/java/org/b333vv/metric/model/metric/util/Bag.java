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
