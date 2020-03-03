package org.b333vv.metric.model.metric;

import org.b333vv.metric.model.code.JavaCode;

import java.util.Set;

public interface Meter<T extends JavaCode> {

    Set<Metric> meter(T t);

}
