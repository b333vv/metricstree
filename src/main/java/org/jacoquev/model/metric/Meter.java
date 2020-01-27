package org.jacoquev.model.metric;

import org.jacoquev.model.code.JavaCode;

import java.util.Set;

public interface Meter<T extends JavaCode> {

    Set<Metric> meter(T t);

}
