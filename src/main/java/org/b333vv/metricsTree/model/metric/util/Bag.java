package org.b333vv.metricsTree.model.metric.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Bag<T> {
    private final Map<T, Integer> contents = new HashMap<T, Integer>();

    public Bag() {
        super();
    }

    public Bag(Bag<T> toCopy) {
        super();
        contents.putAll(toCopy.contents);
    }

    public void add(T obj) {
        final Integer currentValue = contents.get(obj);
        if (currentValue != null) {
            contents.put(obj, currentValue + 1);
        } else {
            contents.put(obj, 1);
        }
    }

    public Set<T> getContents() {
        return contents.keySet();
    }

    public int getCountForObject(T obj) {
        final Integer currentValue = contents.get(obj);
        if (currentValue != null) {
            return currentValue;
        } else {
            return 0;
        }
    }

    public void loadFrom(Bag<T> bag) {
        contents.clear();
        contents.putAll(bag.contents);
    }

    public void clear() {
        contents.clear();
    }
}
